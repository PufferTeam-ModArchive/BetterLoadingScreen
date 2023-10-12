package alexiil.mods.load.coremod;

import net.minecraft.launchwrapper.IClassTransformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import alexiil.mods.load.BetterLoadingScreen;

public class BetterLoadingScreenTransformer implements IClassTransformer, Opcodes {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals("net.minecraft.client.Minecraft")) {
            return transformMinecraft(basicClass);
        }
        if (transformedName.equals("cpw.mods.fml.client.SplashProgress")) {
            return transformSplashProgress(basicClass);
        }
        if (name.equals("com.mumfrey.liteloader.client.api.ObjectFactoryClient")) {
            return transformObjectFactoryClient(basicClass);
        }
        return basicClass;
    }

    private byte[] transformObjectFactoryClient(byte[] before) {
        ClassNode classNode = new ClassNode();
        ClassReader reader = new ClassReader(before);
        reader.accept(classNode, ClassReader.SKIP_DEBUG);
        for (MethodNode m : classNode.methods) {
            if (m.name.equals("preBeginGame")) {
                m.instructions.clear();
                m.instructions.add(new TypeInsnNode(NEW, "alexiil/mods/load/LiteLoaderProgress"));
                m.instructions.add(
                        new MethodInsnNode(
                                INVOKESPECIAL,
                                "alexiil/mods/load/LiteLoaderProgress",
                                "<init>",
                                "()V",
                                false));
                m.instructions.add(new InsnNode(RETURN));
            }
        }
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(cw);
        return cw.toByteArray();
    }

    private byte[] transformSplashProgress(byte[] before) {
        ClassNode classNode = new ClassNode();
        ClassReader reader = new ClassReader(before);
        reader.accept(classNode, ClassReader.SKIP_DEBUG);
        for (MethodNode m : classNode.methods) {
            if (m.name.equals("finish")) {
                m.instructions.insert(
                        new MethodInsnNode(INVOKESTATIC, "alexiil/mods/load/ProgressDisplayer", "close", "()V", false));
            }
        }
        ClassWriter cw = new ClassWriter(0);
        classNode.accept(cw);
        return cw.toByteArray();
    }

    private byte[] transformMinecraft(byte[] before) {
        ClassNode classNode = new ClassNode();
        ClassReader reader = new ClassReader(before);
        reader.accept(classNode, ClassReader.SKIP_DEBUG);
        int transformations = 0;

        for (MethodNode m : classNode.methods) {
            if ((m.name.equals("aj") || m.name.equals("loadScreen")) && m.desc.equals("()V")) {
                m.instructions.clear();
                m.instructions.add(new InsnNode(RETURN));
                m.exceptions.clear();
                m.tryCatchBlocks.clear();
                m.localVariables = null;
                m.visitMaxs(0, 1);
                transformations++;
                break;
            } else if ((m.name.equals("ag") || m.name.equals("startGame")) && m.desc.equals("()V")) {
                for (AbstractInsnNode node : m.instructions.toArray()) {
                    if (node instanceof MethodInsnNode
                            && ((MethodInsnNode) node).owner.equals("cpw/mods/fml/client/FMLClientHandler")
                            && ((MethodInsnNode) node).name.equals("instance")) {
                        m.instructions.insertBefore(
                                node,
                                new MethodInsnNode(
                                        INVOKESTATIC,
                                        "alexiil/mods/load/ProgressDisplayer",
                                        "minecraftDisplayFirstProgress",
                                        "()V",
                                        false));
                        transformations++;
                        break;
                    }
                }
            }
            for (AbstractInsnNode node : m.instructions.toArray()) {
                /*
                 * LiteLoader disabling -NOTE TO ANYONE FROM LITELOADER OR ANYONE ELSE: I am disabling liteloader's
                 * overlay simply because otherwise it switches between liteloader's bar and mine. I can safely assume
                 * that people won't want this, and as my progress bar is the entire mod, they can disable this
                 * behaviour by removing my mod (as all my mod does is just add a loading bar)
                 */
                if (node instanceof MethodInsnNode) {
                    if (((MethodInsnNode) node).owner.equals("com/mumfrey/liteloader/client/gui/startup/LoadingBar")) {
                        m.instructions.remove(node);
                    }
                }
            }
        }

        if (transformations != 2) {
            throw new IllegalStateException("BetterLoadingScreen couldn't not transform Minecraft properly!");
        }
        ClassWriter cw = new ClassWriter(0);
        classNode.accept(cw);
        final byte[] byteArray = cw.toByteArray();
        BetterLoadingScreen.log.debug("Transformed Minecraft");
        return byteArray;
    }
}
