import java.io.Serializable;

public class ArtistName implements Serializable {
	private String artistName;
	private String hashCode;

	public ArtistName(String artistName){
		this.artistName = artistName;
	}
	public ArtistName(String artistName,String hashCode){
		this.artistName = artistName;
		this.hashCode = hashCode;
	}
	
	public String getArtistName(){
		return artistName;
	}
	
	public void setArtistName(){
		this.artistName = artistName;
	}

	public String getHashCode() {
		return hashCode;
	}

	public void setHashCode(String hashCode) {
		this.hashCode = hashCode;
	}
}