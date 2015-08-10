package net.sharkhunter.gmusic;

import java.io.*;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;
import net.pms.dlna.*;

public class GmPMSSong extends DLNAResource{

	private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(GmPMSSong.class);
	private GmSong song;
	private boolean downloading;
	private ByteArrayOutputStream out;

	public GmPMSSong(GmSong song) {
		this.song=song;
		this.downloading=false;
		this.out=new ByteArrayOutputStream();
		if(getMedia()==null) {
			DLNAMediaInfo m=new DLNAMediaInfo();
			DLNAMediaAudio audio=new DLNAMediaAudio();
			audio.setAlbum(song.getAlbum());
			audio.setArtist(song.getArtist());
			audio.setSongname(song.getName());
			ArrayList<DLNAMediaAudio> a=new ArrayList<DLNAMediaAudio>();
			a.add(audio);
			m.setAudioTracksList(a);
			setMedia(m);
		}
	}

	@Override
	public String getName() {
		return this.song.getName();
	}

	@Override
	public String getSystemName() {
		return getName()+".mp3";
	}

	public boolean isUnderlyingSeekSupported() {
		return true;
	}

	@Override
	public void resolve() {
	}

	@Override
	public boolean isValid() {
		resolveFormat();;
		return true;
	}

	@Override
	public long length() {
		return DLNAMediaInfo.TRANS_SIZE;
    }

	@Override
	public boolean isFolder() {
          return false;
    }

	public boolean isSearched() {
		return true;
	}

	@Override
	public InputStream getInputStream() {
		try {
			boolean spawn=true;
			if(!this.downloading) {
				this.downloading=true;
				if(song.delay()==-1)
					spawn=false;
				if(song.save()) {
					OutputStream[] os=new OutputStream[2];
					os[0]=this.out;
					os[1]=new FileOutputStream(song.fileName());
					this.song.download(os,spawn);
				}
				else {
					this.song.download(this.out,spawn);
				}
			}

			if(spawn)
				Thread.sleep(song.delay());
			return new GmByteInputStream(out,(int)song.getLength());
			//return Gs.cache.getInputStream(song.getId());
		}
		catch (Exception e) {
			LOGGER.debug("{GMusic} Song exception occurred: ", e);
			return null;
		}
	}

	public InputStream getThumbnailInputStream() {
		String url=song.getCoverURL();
		try {
			if(url.length()==0)
				return super.getThumbnailInputStream();
			byte[] b=downloadAndSendBinary(url);
			return new ByteArrayInputStream(b);
		}
		catch (Exception e) {
			try {
				return super.getThumbnailInputStream();
			}
			catch (Exception e1) {
				return null;
			}
		}

	}
}
