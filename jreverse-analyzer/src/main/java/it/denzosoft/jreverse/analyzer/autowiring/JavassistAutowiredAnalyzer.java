package it.denzosoft.jreverse.analyzer.autowiring;

import it.denzosoft.jreverse.analyzer.beancreation.BeanCreationAnalyzer;
import it.denzosoft.jreverse.core.model.AutowiredAnalysisResult;
import it.denzosoft.jreverse.core.model.JarContent;
import it.denzosoft.jreverse.core.port.AutowiredAnalyzer;

/**
 * Wrapper for backward compatibility with tests.
 * Delegates to the actual implementation in the autowired package.
 */
public class JavassistAutowiredAnalyzer implements AutowiredAnalyzer {
    
    private final it.denzosoft.jreverse.analyzer.autowired.JavassistAutowiredAnalyzer delegate;
    
    public JavassistAutowiredAnalyzer(BeanCreationAnalyzer beanCreationAnalyzer) {
        this.delegate = new it.denzosoft.jreverse.analyzer.autowired.JavassistAutowiredAnalyzer();
    }
    
    @Override
    public AutowiredAnalysisResult analyzeAutowiring(JarContent jarContent) {
        return delegate.analyzeAutowiring(jarContent);
    }
    
    @Override
    public boolean canAnalyze(JarContent jarContent) {
        return delegate.canAnalyze(jarContent);
    }
}