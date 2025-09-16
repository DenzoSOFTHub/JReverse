package it.denzosoft.jreverse.core.model;

/**
 * Metrics related to coupling between packages or classes.
 * Includes afferent and efferent coupling measures.
 *
 * @author JReverse Development Team
 * @since 1.1.0 (Phase 3)
 */
public class CouplingMetrics {

    private final int afferentCoupling;
    private final int efferentCoupling;
    private final double instability;
    private final double abstractness;
    private final double distance;

    public CouplingMetrics(int afferentCoupling, int efferentCoupling,
                          double instability, double abstractness, double distance) {
        this.afferentCoupling = afferentCoupling;
        this.efferentCoupling = efferentCoupling;
        this.instability = instability;
        this.abstractness = abstractness;
        this.distance = distance;
    }

    public int getAfferentCoupling() { return afferentCoupling; }
    public int getEfferentCoupling() { return efferentCoupling; }
    public double getInstability() { return instability; }
    public double getAbstractness() { return abstractness; }
    public double getDistance() { return distance; }

    @Override
    public String toString() {
        return String.format("CouplingMetrics{Ca=%d, Ce=%d, I=%.2f, A=%.2f, D=%.2f}",
                afferentCoupling, efferentCoupling, instability, abstractness, distance);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int afferentCoupling = 0;
        private int efferentCoupling = 0;
        private double instability = 0.0;
        private double abstractness = 0.0;
        private double distance = 0.0;

        public Builder afferentCoupling(int afferentCoupling) {
            this.afferentCoupling = afferentCoupling;
            return this;
        }

        public Builder efferentCoupling(int efferentCoupling) {
            this.efferentCoupling = efferentCoupling;
            return this;
        }

        public Builder instability(double instability) {
            this.instability = instability;
            return this;
        }

        public Builder abstractness(double abstractness) {
            this.abstractness = abstractness;
            return this;
        }

        public Builder distance(double distance) {
            this.distance = distance;
            return this;
        }

        public CouplingMetrics build() {
            return new CouplingMetrics(afferentCoupling, efferentCoupling,
                                     instability, abstractness, distance);
        }
    }
}