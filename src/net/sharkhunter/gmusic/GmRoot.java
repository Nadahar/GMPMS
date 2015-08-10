package net.sharkhunter.gmusic;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import net.pms.PMS;
import net.pms.dlna.virtual.VirtualFolder;


public class GmRoot extends VirtualFolder{
	private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(GmRoot.class);
	private Gm gmObj;

	private void setDelay() {
		String delay=(String)PMS.getConfiguration().getCustomProperty("gm_plugin.init_delay");
		if(delay!=null) {
			try {
				gmObj.delay=Integer.parseInt(delay);
			}
			catch (Exception e) {
				LOGGER.warn("{GMusic} Illegal init_delay value \"{}\": {}", delay, e);
			}
		}
	}

	private void setMaxDisplay() {
		String disp=(String)PMS.getConfiguration().getCustomProperty("gm_plugin.max_display");
		if(disp!=null) {
			try {
				Gm.DisplayLimit=Integer.parseInt(disp);
			}
			catch (Exception e) {
				LOGGER.warn("{GMusic} Illegal max_display value \"{}\": {}", disp, e);
			}
		}
	}

	private void setPath(String privDbg) {
		try {
			File saveFolder=new File(PMS.getConfiguration().getTempFolder(),"gm_plugin");
			String confPath=(String)PMS.getConfiguration().getCustomProperty("gm_plugin.path");
			String path;
			if(confPath==null) {
				saveFolder.mkdir();
				path=saveFolder.toString();
			}
			else
				path=confPath;
			gmObj.setPath(path);
		}
		catch (Exception e) {
			LOGGER.warn("{GMusic} Could not set gs path correctly: {}", e);
		}
	}

	public static String credPath() {
		return (String) PMS.getConfiguration().getCustomProperty("cred.path");
	}

	private Gm createGmObj() {
		String cPath=credPath();
		if(StringUtils.isEmpty(cPath))
			return null;
		File f=new File(cPath);
		if(!f.exists())
			return null;
		try {
			BufferedReader in = new BufferedReader(new FileReader(f));
			try {
				String str;
				while ((str = in.readLine()) != null) {
					str=str.trim();
					if(StringUtils.isEmpty(str)||str.startsWith("#"))
						continue;
					String[] s=str.split("\\s*=\\s*",2);
					if(s.length<2)
						continue;
					if(!s[0].equalsIgnoreCase("gmusic"))
						continue;
					String[] s2=s[1].split(",",2);
					if(s2.length<2)
						continue;
					return new Gm(s2[0],s2[1]);
				}
			} finally {
				in.close();
			}
		}
    	catch (Exception e) {
    	}
		return null;
	}

	private void setConfig() {
		String privDbg=(String)PMS.getConfiguration().getCustomProperty("gm_plugin.private_dbg");
		setDelay();
		setPath(privDbg);
		setMaxDisplay();
		String save=(String)PMS.getConfiguration().getCustomProperty("gm_plugin.xxx.yyy.sAvE");
		String zero=(String)PMS.getConfiguration().getCustomProperty("gm_plugin.zero_fill");
		if(save!=null) {
			gmObj.setSave(true);
		}
		if(zero!=null&&zero.length()>0)
			if(zero.equalsIgnoreCase("true"))
				gmObj.zero_fill=true;
	}

	public GmRoot() throws Exception {
		super("GoogleMusic",null);
		this.gmObj=createGmObj();
		if(gmObj==null) {
			LOGGER.error("{GMusic} Missing plugin credentials");
			throw new Exception("Init error");
		}
		if(!StringUtils.isEmpty(gmObj.initError)) {
			LOGGER.error("{GMusic} {}", gmObj.initError);
			throw new Exception("Init error");
		}
		setConfig();

		String info="Gm 0.11 using path "+gmObj.savePath;
		LOGGER.info("{GMusic} {}", info);
	}

	public void init() {
		addChild(new VirtualFolder("All Songs",null) {
			public void discoverChildren()  {
				String page=gmObj.request("", "loadalltracks");
				GmSong[] songs=GmSong.parseSongs(page, gmObj);
				//PMS.debug("alb disc song "+songs.length);
				int j=0;
				for(int i=0;i<songs.length;i++) {
					if(Gm.more(j)) {
						addChild(new GmMore(songs,i));
						break;
					}
					j++;
					addChild(new GmPMSSong(songs[i]));
				}
			}
		}
		);
		addChild(new VirtualFolder("Playlists",null) {
			public void discoverChildren()  {
				String page=gmObj.request("", "loadplaylist");
				GmPlaylist[] pls=GmPlaylist.parsePlaylist(page, gmObj);
				//PMS.debug("alb disc song "+songs.length);
				int j=0;
				for(int i=0;i<pls.length;i++) {
					if(Gm.more(j)) {
						addChild(new GmMore(pls,i));
						break;
					}
					j++;
					addChild(new GmPMSPlaylist(pls[i]));
				}
			}
		}
		);
	}
}
