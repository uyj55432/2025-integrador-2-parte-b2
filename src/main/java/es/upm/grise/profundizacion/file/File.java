package es.upm.grise.profundizacion.file;

import java.util.ArrayList;
import java.util.List;

import es.upm.grise.profundizacion.exceptions.EmptyBytesArrayException;
import es.upm.grise.profundizacion.exceptions.InvalidContentException;
import es.upm.grise.profundizacion.exceptions.WrongFileTypeException;

public class File {

    private FileType type;
    private List<Character> content;

	/*
	 * Constructor
	 */
    public File() {

        this.content = new ArrayList<Character>();
        
    }

	/*
	 * Method to code / test
	 */
    public void addProperty(char[] newcontent) throws InvalidContentException, WrongFileTypeException {

        if (newcontent == null) {
        	
            throw new InvalidContentException();
            
        }

        if (type == FileType.IMAGE) {
        	
            throw new WrongFileTypeException();
            
        }

        for (char c : newcontent) {
        	
            this.content.add(c);
            
        }
    }

	/*
	 * Method to code / test
	 */
    public long getCRC32() throws EmptyBytesArrayException {
    	
        if (this.content.isEmpty()) {
        	
            return 0L;
            
        }

        byte[] bytes = new byte[content.size()];
        for (int i = 0; i < content.size(); i++) {
        	
            char c = content.get(i);
            bytes[i * 2] = (byte) ((c >>> 8) & 0xFF);
            bytes[i * 2 + 1] = (byte) (c & 0xFF);
            
        }
        
        return new FileUtils().calculateCRC32(bytes);
    }
    
    
	/*
	 * Setters/getters
	 */
    public void setType(FileType type) {
    	
    	this.type = type;
    	
    }
    
    public List<Character> getContent() {
    	
    	return content;
    	
    }
    
}
