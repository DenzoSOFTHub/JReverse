package it.denzosoft.jreverse.analyzer.beandefinition;

import java.util.Set;

/**
 * Information about the lifecycle of a Spring bean.
 */
public class BeanLifecycleInfo {

    public enum LifecycleHookType {
        INIT, DESTROY, DEPENDS_ON
    }

    public enum LifecycleHookSource {
        ANNOTATION, XML, JAVA_CONFIG
    }

    private final String beanName;
    private final boolean hasPostConstruct;
    private final boolean hasPreDestroy;
    private final Set<String> dependsOn;
    private final int initializationOrder;

    private BeanLifecycleInfo(String beanName,
                             boolean hasPostConstruct,
                             boolean hasPreDestroy,
                             Set<String> dependsOn,
                             int initializationOrder) {
        this.beanName = beanName;
        this.hasPostConstruct = hasPostConstruct;
        this.hasPreDestroy = hasPreDestroy;
        this.dependsOn = dependsOn != null ? Set.copyOf(dependsOn) : Set.of();
        this.initializationOrder = initializationOrder;
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters
    public String getBeanName() { return beanName; }
    public boolean hasPostConstruct() { return hasPostConstruct; }
    public boolean hasPreDestroy() { return hasPreDestroy; }
    public Set<String> getDependsOn() { return dependsOn; }
    public int getInitializationOrder() { return initializationOrder; }

    public LifecycleHookType getHookType() {
        if (hasPostConstruct) return LifecycleHookType.INIT;
        if (hasPreDestroy) return LifecycleHookType.DESTROY;
        if (!dependsOn.isEmpty()) return LifecycleHookType.DEPENDS_ON;
        return LifecycleHookType.INIT; // Default
    }

    public LifecycleHookSource getHookSource() {
        // For now, assume all are annotation-based since we're using @PostConstruct/@PreDestroy
        return LifecycleHookSource.ANNOTATION;
    }

    public static class Builder {
        private String beanName;
        private boolean hasPostConstruct = false;
        private boolean hasPreDestroy = false;
        private Set<String> dependsOn = Set.of();
        private int initializationOrder = 0;

        public Builder beanName(String beanName) {
            this.beanName = beanName;
            return this;
        }

        public Builder hasPostConstruct(boolean hasPostConstruct) {
            this.hasPostConstruct = hasPostConstruct;
            return this;
        }

        public Builder hasPreDestroy(boolean hasPreDestroy) {
            this.hasPreDestroy = hasPreDestroy;
            return this;
        }

        public Builder dependsOn(Set<String> dependsOn) {
            this.dependsOn = dependsOn;
            return this;
        }

        public Builder initializationOrder(int initializationOrder) {
            this.initializationOrder = initializationOrder;
            return this;
        }

        public BeanLifecycleInfo build() {
            return new BeanLifecycleInfo(
                beanName,
                hasPostConstruct,
                hasPreDestroy,
                dependsOn,
                initializationOrder
            );
        }
    }
}