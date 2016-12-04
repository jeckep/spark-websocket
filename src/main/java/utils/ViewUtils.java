package utils;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import spark.template.velocity.VelocityTemplateEngine;

/**
 * Created by jeckep on 04.12.16.
 */
public class ViewUtils {
    public static VelocityTemplateEngine strictVelocityEngine() {
        VelocityEngine configuredEngine = new VelocityEngine(){
            @Override
            public Template getTemplate(String name) throws ResourceNotFoundException, ParseErrorException {
                return super.getTemplate(name, "UTF-8");
            }
        };
        configuredEngine.setProperty(VelocityEngine.INPUT_ENCODING, "UTF-8");
        configuredEngine.setProperty(VelocityEngine.OUTPUT_ENCODING, "UTF-8");
        configuredEngine.setProperty(VelocityEngine.ENCODING_DEFAULT, "UTF-8");
        configuredEngine.setProperty("runtime.references.strict", true);
        configuredEngine.setProperty("resource.loader", "class");
        configuredEngine.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        return new VelocityTemplateEngine(configuredEngine);
    }
}
