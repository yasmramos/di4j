
// import com.univsoftdev.di4j.AppConfig; // Commented out due to missing AppConfig.java
import com.univsoftdev.di4j.Configuration;
import com.univsoftdev.di4j.Injector;

public class Main {
    
    public static void main(String[] args) {
        String propertiesFile = "application.properties";
        
        Injector injector = new Injector(new Configuration(), propertiesFile);
        // injector.register(AppConfig.class); // Commented out due to missing AppConfig.java
        
        // AppConfig resolve = injector.resolve(AppConfig.class); // Commented out due to missing AppConfig.java
        // System.out.println(resolve.getAppName()); // Commented out due to missing AppConfig.java
        System.out.println("Injector created. AppConfig related lines commented out."); // Placeholder
    }
}
