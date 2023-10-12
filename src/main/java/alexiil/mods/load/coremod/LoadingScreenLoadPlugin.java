package alexiil.mods.load.coremod;

import java.io.File;
import java.util.Map;

import alexiil.mods.load.ProgressDisplayer;
import alexiil.mods.load.Translation;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.TransformerExclusions({ "alexiil.mods.load.coremod" })
public class LoadingScreenLoadPlugin implements IFMLLoadingPlugin {

    @Override
    public String[] getASMTransformerClass() {
        return new String[] { BetterLoadingScreenTransformer.class.getName() };
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        File coremodLocation = (File) data.get("coremodLocation");
        Translation.addTranslations(coremodLocation);
        ProgressDisplayer.start(coremodLocation);
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
