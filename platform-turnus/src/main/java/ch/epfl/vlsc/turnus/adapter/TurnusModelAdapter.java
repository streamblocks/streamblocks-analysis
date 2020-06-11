package ch.epfl.vlsc.turnus.adapter;

import se.lth.cs.tycho.attribute.GlobalNames;
import se.lth.cs.tycho.attribute.Types;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.SourceFile;
import se.lth.cs.tycho.compiler.SourceUnit;
import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.decl.LocalVarDecl;
import se.lth.cs.tycho.ir.decl.ParameterVarDecl;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.cal.InputPattern;
import se.lth.cs.tycho.ir.entity.cal.OutputExpression;
import se.lth.cs.tycho.ir.network.Connection;
import se.lth.cs.tycho.ir.network.Instance;
import se.lth.cs.tycho.ir.type.TypeExpr;
import se.lth.cs.tycho.type.*;
import turnus.common.TurnusException;
import turnus.common.util.FileUtils;
import turnus.model.dataflow.Type;
import turnus.model.dataflow.*;
import turnus.model.versioning.Version;
import turnus.model.versioning.Versioner;

import java.io.File;
import java.util.*;

public class TurnusModelAdapter {
    public static final String TURNUS_MODEL = "turnus.profiler.streamblocks.wrapper";

    private final CompilationTask task;
    private final Versioner versioner;
    private final Network network;
    private final Types types;
    private final GlobalNames globalNames;
    private final Map<String, CalActor> calActorMap;

    public TurnusModelAdapter(CompilationTask task, Versioner versioner) {
        this.task = task;
        this.versioner = versioner;
        this.calActorMap = new HashMap<>();
        this.types = task.getModule(Types.key);
        this.globalNames = task.getModule(GlobalNames.key);
        this.network = createNetwork(task.getNetwork());
    }

    public Network getNetwork() {
        return network;
    }

    private Network createNetwork(se.lth.cs.tycho.ir.network.Network tychoNetwork) {
        Date now = new Date();

        DataflowFactory factory = DataflowFactory.eINSTANCE;
        Network network = factory.createNetwork();
        network.setProject(task.getIdentifier().toString());
        network.setSourceFile(task.getIdentifier().toString());
        network.setName(task.getIdentifier().toString());

        Version version = versioner.getVersion(getSourceFile(task.getIdentifier()));
        version.setVersionDate(now);
        network.setVersion(version);

        for (Instance instance : tychoNetwork.getInstances()) {
            GlobalEntityDecl entityDecl = globalNames.entityDecl(instance.getEntityName(), true);
            assert entityDecl.getEntity() instanceof CalActor;
            Optional<QID> actorQID = globalNames.globalName(entityDecl);
            String fileName = "";
            if (actorQID.isPresent()) {
                Optional<SourceUnit> calSourceFile = task.getSourceUnit(actorQID.get());
                if (calSourceFile.isPresent()) {
                    if (!calSourceFile.get().isSynthetic()) {
                        SourceFile sourceFile = (SourceFile) calSourceFile.get();
                        fileName = sourceFile.getFile().toAbsolutePath().toString();
                    }
                }

            }

            CalActor calActor = (CalActor) entityDecl.getEntity();
            calActorMap.put(instance.getInstanceName(), calActor);

            String className = actorQID.toString();
            ActorClass actorClass = network.getActorClass(className);
            // -- if the actor-class is not yet defined, create a new one
            if (actorClass == null) {
                actorClass = factory.createActorClass();
                actorClass.setName(className);
                actorClass.setSourceFile(fileName);
                actorClass.setNameSpace(actorQID.get().getButLast().toString());
                actorClass.setSourceCode(getSourceCode(fileName));
                network.getActorClasses().add(actorClass);

                version = versioner.getVersion(getSourceFile(actorQID));
                version.setVersionDate(now);
                actorClass.setVersion(version);
            }

            // -- Actor Instance
            Actor actor = factory.createActor();
            actor.setName(instance.getInstanceName());
            actor.setActorClass(actorClass);
            network.getActors().add(actor);

            // -- Actor Input Ports
            for (PortDecl decl : calActor.getInputPorts()) {
                Port port = factory.createPort();
                port.setName(decl.getName());
                actor.getInputPorts().add(port);
            }

            // -- Actor Output Ports
            for (PortDecl decl : calActor.getOutputPorts()) {
                Port port = factory.createPort();
                port.setName(decl.getName());
                actor.getOutputPorts().add(port);
            }

            // -- Actions
            Set<se.lth.cs.tycho.ir.entity.cal.Action> allActions = new HashSet<>();
            allActions.addAll(calActor.getActions());
            allActions.addAll(calActor.getInitializers());
            for (se.lth.cs.tycho.ir.entity.cal.Action tychoAction : allActions) {
                Action action = factory.createAction();
                action.setName(tychoAction.getTag().toString());
                actor.getActions().add(action);

                for (InputPattern pattern : tychoAction.getInputPatterns()) {
                    Port port = actor.getInputPort(pattern.getPort().getName());
                    action.getInputPorts().add(port);
                }

                for (OutputExpression output : tychoAction.getOutputExpressions()) {
                    Port port = actor.getOutputPort(output.getPort().getName());
                    action.getOutputPorts().add(port);
                }
            }

            // -- State Variables
            for (LocalVarDecl localVarDecl : calActor.getVarDecls()) {
                Variable stateVar = factory.createVariable();
                stateVar.setName(localVarDecl.getName());
                stateVar.setType(getTurnusType(localVarDecl.getType()));
                actor.getVariables().add(stateVar);
            }

            // -- Parameters Variables as State Variables
            for (ParameterVarDecl parameterVarDecl : calActor.getValueParameters()) {
                Variable paramVar = factory.createVariable();
                paramVar.setName(parameterVarDecl.getName());
                paramVar.setType(getTurnusType(parameterVarDecl.getType()));
                actor.getVariables().add(paramVar);
            }
        }

        // -- FIFO Buffers
        for (Connection connection : tychoNetwork.getConnections()) {
            Connection.End source = connection.getSource();
            Connection.End target = connection.getTarget();

            String instanceSource = source.getInstance().get();
            String portSource = source.getPort();

            String instanceTarget = target.getInstance().get();
            String portTarget = target.getPort();

            Port tpSource = network.getActor(instanceSource).getOutputPort(portSource);
            Port tpTarget = network.getActor(instanceTarget).getInputPort(portTarget);

            Buffer buffer = factory.createBuffer();
            buffer.setSource(tpSource);
            buffer.setTarget(tpTarget);

            CalActor actor = calActorMap.get(instanceSource);
            buffer.setType(getOutputPortType(actor, portSource));
            network.getBuffers().add(buffer);

        }

        return network;
    }

    private Type getOutputPortType(CalActor calActor, String portName) {
        Optional<PortDecl> port = calActor.getOutputPorts().stream().filter(portDecl -> portDecl.getName().equals(portName)).findFirst();
        if (port.isPresent()) {
            Type type = getTurnusType(port.get().getType());
            return type;
        }
        return DataflowFactory.eINSTANCE.createTypeUndefined();
    }

    private Type getTurnusType(TypeExpr expr) {
        se.lth.cs.tycho.type.Type tychoType = types.type(expr);
        return getTurnusType(tychoType);
    }

    private Type getTurnusType(se.lth.cs.tycho.type.Type tychoType) {
        if (tychoType instanceof IntType) {
            IntType intType = (IntType) tychoType;
            int size = intType.getSize().isPresent() ? intType.getSize().getAsInt() : 32;
            if (intType.isSigned()) {
                TypeInt type = DataflowFactory.eINSTANCE.createTypeInt();
                type.setSize(size);
                return type;
            } else {
                TypeUint type = DataflowFactory.eINSTANCE.createTypeUint();
                type.setSize(size);
                return type;
            }
        } else if (tychoType instanceof BoolType) {
            return DataflowFactory.eINSTANCE.createTypeBoolean();
        } else if (tychoType instanceof RealType) {
            RealType realType = (RealType) tychoType;

            TypeDouble typeDouble = DataflowFactory.eINSTANCE.createTypeDouble();
            typeDouble.setSize(realType.getSize());
            return typeDouble;
        } else if (tychoType instanceof StringType) {
            return DataflowFactory.eINSTANCE.createTypeString();
        } else if (tychoType instanceof ListType) {
            ListType listType = (ListType) tychoType;

            TypeList typeList = DataflowFactory.eINSTANCE.createTypeList();
            if (listType.getSize().isPresent()) {
                typeList.setElements(listType.getSize().getAsInt());
            }
            typeList.setListType(getTurnusType(listType.getElementType()));
            return typeList;
        } else {
            return DataflowFactory.eINSTANCE.createTypeUndefined();
        }
    }

    private String getSourceCode(String fileName) {
        try {
            File file = new File(fileName);
            return FileUtils.toString(file);
        } catch (TurnusException e) {
        }
        return "";
    }

    private File getSourceFile(Optional<QID> entityQID) {
        String fileName = "";
        if (entityQID.isPresent()) {
            Optional<SourceUnit> calSourceFile = task.getSourceUnit(entityQID.get());
            if (calSourceFile.isPresent()) {
                if (!calSourceFile.get().isSynthetic()) {
                    SourceFile sourceFile = (SourceFile) calSourceFile.get();
                    fileName = sourceFile.getFile().toAbsolutePath().toString();
                    return new File(fileName);
                }
            }
        }
        return null;
    }

    private File getSourceFile(QID entityQID) {
        String fileName = "";
        Optional<SourceUnit> calSourceFile = task.getSourceUnit(entityQID);
        if (calSourceFile.isPresent()) {
            if (!calSourceFile.get().isSynthetic()) {
                SourceFile sourceFile = (SourceFile) calSourceFile.get();
                fileName = sourceFile.getFile().toAbsolutePath().toString();
                return new File(fileName);
            }
        }
        return null;
    }


}
