/**
 * Config.java
 * @author Peter Dalbadie
 * 21-Sep-2004
 * 
 */

package org.prorefactor.core.unittest;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.prorefactor.core.schema.Schema;

import com.joanju.ProparseLdr;

/**
 * Configurator for Proparse.
 * This is a singleton. Use it to configure proparse to local
 * environment. Uses configuration values stored in the properties
 * file identified by ENV_PROPERTIES.
 */
public class Config {
	
	private static final String ENV_PROPERTIES = "data/test.properties";
	
	private static Properties envProp;
	
	private static Config instance = null;

	private static ProparseLdr parser = ProparseLdr.getInstance();
	
	private static Schema schema = Schema.getInstance();

	public static void configParser() throws IOException {
		parser.configSet("init","false");
		
		//schema = Schema.deleteInstance();
		schema.loadSchema(getEnvProperty("schema_file"));
		
		parser.configSet("batch-mode", getEnvProperty("batch_mode"));
		parser.configSet("keyword-all", getEnvProperty("keywordall"));
		parser.configSet("opsys", getEnvProperty("opsys"));
		parser.configSet("propath", getEnvProperty("propath"));
		parser.configSet("proversion", getEnvProperty("proversion"));
		parser.configSet("window-system", getEnvProperty("window_system"));
	
		// dbAliases();
		
		parser.configSet("init","true");
	}

	/**
	 * @return
	 */
	private void dbAliases() {
		parser.schemaAliasDelete(""); // deletes all
		String [] alias = getEnvProperty("database_aliases").split(",");
		
		for (int i = 0; i < alias.length - 1; i = i + 2) {
			parser.schemaAliasCreate(alias[i], alias[i+1]);
		}
	}
	
	/**
	 * Format a string as a directory for concatenation.
	 * @param string - a path.
	 * @return the path with a single ending slash.
	 */
	private static String dir(String string) {
		if (string.endsWith("/")){
			return string;
		}
		else {
			return string + "/";
		}
	}


	public static String getEnvProperty(String propertyName) {

		if (envProp == null) {
			envProp = new Properties();
			try {
				envProp.load(new FileInputStream(ENV_PROPERTIES));
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			}
		}
		return envProp.getProperty(propertyName);
	}

	public static Config getInstance(){
		if (instance == null){
			instance = new Config();
			try {
				configParser();
			} 
			catch (Exception e){
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			}
		}
		return instance;
	}

	public static String testDir(){
		return dir(getEnvProperty("test_dir"));
	}
	

}