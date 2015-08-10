package net.sharkhunter.gmusic;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import net.pms.PMS;
import net.pms.configuration.Build;
import net.pms.dlna.DLNAResource;
import net.pms.external.AdditionalFolderAtRoot;
import javax.swing.*;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;

public class Gm_plugin implements AdditionalFolderAtRoot {

	private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Gm_plugin.class);
	private GmRoot gmRoot;

	public Gm_plugin() {
		try {
			gmRoot=null;
			gmRoot=new GmRoot();
			gmRoot.init();
		}
		catch (Exception e) {
			LOGGER.debug("{GMusic} Exception: {}", e);
		}
	}

	public DLNAResource getChild() {
		return gmRoot;
	}

	public void shutdown() {
	}

	public String name() {
		return "GoogleMusic";
	}

	//@Override
	public JComponent config() {
		return null;
	}

	private static void writeCred(String path) throws IOException {
		File f=new File(path);
		BufferedWriter wr=new BufferedWriter(new FileWriter(f,true));
		wr.write("# Enter your GoogleMusic credientials below\n");
		wr.write("#gmusic=user,pwd\n");
		wr.flush();
		wr.close();
	}

	public static void postInstall() {
		String cPath=GmRoot.credPath();
		if(StringUtils.isEmpty(cPath)) {
			cPath=Build.getProfileDirectoryName()+File.separator+"UMS.cred";
			PMS.getConfiguration().setCustomProperty("cred.path", cPath);
			try {
				PMS.getConfiguration().save();
			} catch (ConfigurationException e) {
				return;
			}
		}
		try {
			writeCred(cPath);
		} catch (IOException e) {
		}
	}
}
