package net.ltxprogrammer.changed.client.tfanimations;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class SplittingSource {
    private static final BiConsumer<EntityGeometry.Cube, EntityGeometry.Cube> DONT_CARE = (t, u) -> {};

    private final Vector3fc originalWantedMin; // The original min this source was created to want
    private final Vector3fc originalWantedMax; // The original max this source was created to want
    private final EntityGeometry.Cube originalSource;
    private EntityGeometry.Cube source = null; // The current cube this source was split to
    private @NotNull BiConsumer<EntityGeometry.Cube, EntityGeometry.Cube> resizeConsumer = DONT_CARE;

    private Direction.Axis splitAxis = null;
    private List<SplittingSource> sources = null;
    private boolean clonedFlag = false; // Indicates this source was actually cloned, to properly compute mass

    public SplittingSource(EntityGeometry.Cube source, Vector3fc originalWantedMin, Vector3fc originalWantedMax) {
        this.originalWantedMin = originalWantedMin;
        this.originalWantedMax = originalWantedMax;
        this.originalSource = source;
        this.source = source;
    }

    public void setResizeConsumer(@Nullable BiConsumer<EntityGeometry.Cube, EntityGeometry.Cube> consumer) {
        if (consumer == null)
            this.resizeConsumer = DONT_CARE;
        else
            this.resizeConsumer = consumer;
    }

    public @Nullable SplittingSource findSourceFor(EntityGeometry.Cube cube) {
        if (this.source == cube)
            return this;
        if (sources == null)
            return null;
        for (var source : sources) {
            var found = source.findSourceFor(cube);
            if (found != null)
                return found;
        }
        return null;
    }

    private static class Empty extends SplittingSource {
        private Empty() {
            super(null, new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, 0.0f));
        }

        @Override
        protected float rateSplitDesireLocal(Vector3fc wantedMin, Vector3fc wantedMax) {
            return Float.MAX_VALUE;
        }

        @Override
        public float rateSplitDesire(Vector3fc wantedMin, Vector3fc wantedMax) {
            return Float.MAX_VALUE;
        }

        @Override
        public EntityGeometry.Cube splitFor(Vector3fc wantedMin, Vector3fc wantedMax, BiConsumer<EntityGeometry.Cube, EntityGeometry.Cube> resizeConsumer) {
            throw new IllegalStateException("Attempting to split an empty source");
        }
    }

    private static final SplittingSource EMPTY = new Empty();

    boolean isLeaf() {
        return sources == null;
    }

    public static SplittingSource empty() {
        return EMPTY;
    }

    public static SplittingSource shallowCopy(SplittingSource source) {
        var splittingSource = new SplittingSource(source.originalSource, source.originalWantedMin, source.originalWantedMax);
        splittingSource.resizeConsumer = source.resizeConsumer;
        splittingSource.source = source.source;
        return splittingSource;
    }

    public static SplittingSource forSplit(EntityGeometry.Cube source,
                                           Vector3fc wantedMin, Vector3fc wantedMax,
                                           BiConsumer<EntityGeometry.Cube, EntityGeometry.Cube> resizeConsumer) {
        var splittingSource = new SplittingSource(source, wantedMin, wantedMax);
        splittingSource.resizeConsumer = resizeConsumer;
        return splittingSource;
    }

    public static SplittingSource forCube(EntityGeometry.Cube source, BiConsumer<EntityGeometry.Cube, EntityGeometry.Cube> resizeConsumer) {
        var splittingSource = new SplittingSource(source, source.getMin(), source.getMax());
        splittingSource.resizeConsumer = resizeConsumer;
        return splittingSource;
    }

    public static SplittingSource forSourceCubes(List<EntityGeometry.Cube> cubes) {
        if (cubes.isEmpty())
            return empty();
        var splittingSource = new SplittingSource(null, new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, 0.0f));
        splittingSource.sources = new ArrayList<>(cubes.size());
        for (var cube : cubes) {
            splittingSource.sources.add(SplittingSource.forCube(cube, DONT_CARE));
        }
        return splittingSource;
    }

    public static SplittingSource forCubes(List<Pair<EntityGeometry.Cube, BiConsumer<EntityGeometry.Cube, EntityGeometry.Cube>>> cubes) {
        var splittingSource = new SplittingSource(null, new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, 0.0f));
        splittingSource.sources = new ArrayList<>(cubes.size());
        for (var cube : cubes) {
            splittingSource.sources.add(SplittingSource.forCube(cube.getFirst(), cube.getSecond()));
        }
        return splittingSource;
    }

    public static SplittingSource forSources(SplittingSource... sources) {
        if (sources.length == 0)
            return empty();
        var splittingSource = new SplittingSource(null, new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, 0.0f));
        splittingSource.sources = new ArrayList<>(sources.length);
        splittingSource.sources.addAll(List.of(sources));
        return splittingSource;
    }

    public static SplittingSource forSources(List<SplittingSource> sources) {
        if (sources.isEmpty())
            return empty();
        var splittingSource = new SplittingSource(null, new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, 0.0f));
        splittingSource.sources = new ArrayList<>(sources.size());
        splittingSource.sources.addAll(sources);
        return splittingSource;
    }

    private EntityGeometry.Cube cloneFor(Vector3fc wantedMin, Vector3fc wantedMax,
                                         BiConsumer<EntityGeometry.Cube, EntityGeometry.Cube> resizeConsumer) {
        if (!isLeaf() && !this.clonedFlag)
            throw new IllegalStateException("cloneFor() called on a splitting source that is not a leaf node, or a previously cloned source");

        if (isLeaf()) {
            this.clonedFlag = true;

            EntityGeometry.Cube resultCube = new EntityGeometry.Cube(this.source);

            sources = new ArrayList<>(2);

            sources.add(SplittingSource.shallowCopy(this));
            sources.add(SplittingSource.forSplit(resultCube, wantedMin, wantedMax, resizeConsumer));

            this.resizeConsumer = DONT_CARE;

            return resultCube;
        } else {
            EntityGeometry.Cube resultCube = new EntityGeometry.Cube(this.source);

            this.sources.add(SplittingSource.forSplit(resultCube, wantedMin, wantedMax, resizeConsumer));

            return resultCube;
        }
    }

    private EntityGeometry.Cube resizeFor(Vector3fc wantedMin, Vector3fc wantedMax,
                                          BiConsumer<EntityGeometry.Cube, EntityGeometry.Cube> resizeConsumer) {
        if (this.splitAxis == null)
            throw new IllegalStateException("resizeFor() called on a splitting source that has a null splitAxis");

        Vector3f wantedSize = new Vector3f();
        Vector3f wantedCenter = new Vector3f();
        wantedMax.sub(wantedMin, wantedSize);
        wantedMax.add(wantedMin, wantedCenter).mul(0.5f);
        float wantedMass = wantedSize.x * wantedSize.y * wantedSize.z;

        float totalMass = wantedMass;
        List<Pair<Float, SplittingSource>> masses = new ArrayList<>(sources.size() + 1);
        for (var source : sources) {
            float mass = source.getMass();
            totalMass += mass;
            masses.add(Pair.of(mass, source));
        }

        int axisIndex = 0;
        for (int i = 0; i < sources.size(); i++) {
            Vector3f deltaCenter = sources.get(i).getCenterOfMass().sub(wantedCenter);
            if ((float)splitAxis.choose(deltaCenter.x, deltaCenter.y, deltaCenter.z) > 0.0f) {
                masses.add(i, Pair.of(wantedMass, null));
                axisIndex = i;
            }
        }

        EntityGeometry.Cube resultCube = null;
        var splittingCube = this.source;
        for (var mass : masses) {
            var splits = splittingCube.splitOnAxis(splitAxis, (totalMass - mass.getFirst()) / totalMass);

            if (mass.getSecond() != null) {
                mass.getSecond().acceptNewSize(splits.getFirst());
            } else {
                resultCube = splits.getFirst();
                var subSource = SplittingSource.forSplit(splits.getFirst(), wantedMin, wantedMax, resizeConsumer);
                sources.add(axisIndex, subSource);
            }

            splittingCube = splits.getSecond();
        }

        return resultCube;
    }

    private void acceptNewSize(EntityGeometry.Cube newCube) {
        resizeConsumer.accept(this.source, newCube);
        this.source = newCube;

        // TODO resize splits down the line
    }

    private @Nullable Direction.Axis findBestAxisLocal(Vector3fc wantedMin, Vector3fc wantedMax) {
        return null; // TODO identify axis for splitting
    }

    /**
     * Allocates a cube for the wanted min/max
     * @param wantedMin wanted minimum
     * @param wantedMax wanted maximum
     * @param resizeConsumer consumer for the results when another cube splits down the line, where the first param is the original cube
     * @return the allocated cube
     */
    public EntityGeometry.Cube splitFor(Vector3fc wantedMin, Vector3fc wantedMax,
                  BiConsumer<EntityGeometry.Cube, EntityGeometry.Cube> resizeConsumer) {
        if (isLeaf()) {
            // Split on closest axis

            Vector3f wantedSize = new Vector3f();
            wantedMax.sub(wantedMin, wantedSize);
            float wantedMass = wantedSize.x * wantedSize.y * wantedSize.z;
            float thisMass = this.getWantedMass();
            float splitRatio = Mth.clamp(thisMass / wantedMass, 0.0f, 1.0f);

            if (splitRatio <= 0.0f || splitRatio >= 1.0f) {
                return cloneFor(wantedMin, wantedMax, resizeConsumer);
            }

            var axis = findBestAxisLocal(wantedMin, wantedMax);
            if (axis == null)
                return cloneFor(wantedMin, wantedMax, resizeConsumer);

            this.splitAxis = axis;
            this.sources = new ArrayList<>(2);

            sources.add(SplittingSource.shallowCopy(this));

            this.resizeConsumer = DONT_CARE;

            return resizeFor(wantedMin, wantedMax, resizeConsumer);
        } else {
            float mostWantedValue = Float.MAX_VALUE;
            SplittingSource mostWanted = null;
            for (var source : sources) {
                float value = source.rateSplitDesire(wantedMin, wantedMax);
                if (value < mostWantedValue || mostWanted == null) {
                    mostWanted = source;
                    mostWantedValue = value;
                }
            }

            if (this.rateSplitDesire(wantedMin, wantedMax) < mostWantedValue || mostWanted == null) {
                // This source wants to split the most
                if (this.clonedFlag)
                    return cloneFor(wantedMin, wantedMax, resizeConsumer);
                else {
                    return resizeFor(wantedMin, wantedMax, resizeConsumer);
                }
            } else {
                return mostWanted.splitFor(wantedMin, wantedMax, resizeConsumer);
            }
        }
    }

    protected float rateSplitDesireLocal(Vector3fc wantedMin, Vector3fc wantedMax) {
        float bonus = 0.0f;
        if (findBestAxisLocal(wantedMin, wantedMax) == splitAxis && splitAxis != null)
            bonus -= 100.0f;

        Vector3f wantedSize = new Vector3f();
        Vector3f wantedCenter = new Vector3f();
        wantedMax.sub(wantedMin, wantedSize);
        wantedMax.add(wantedMin, wantedCenter).mul(0.5f);

        Vector3f thisSize = this.getMax().sub(this.getMin());
        Vector3f thisCenter = this.getCenterOfMass();
        wantedMax.sub(wantedMin, wantedSize);

        float lowest = Float.MAX_VALUE;
        for (var normal : Direction.values()) {
            Vector3f wantedSurfaceCenter = new Vector3f(wantedCenter).add(normal.step().mul(wantedSize).mul(0.5f));
            Vector3f thisSurfaceCenter = new Vector3f(thisCenter).add(normal.step().mul(thisSize).mul(0.5f));

            float distance = thisSurfaceCenter.distance(wantedSurfaceCenter);
            if (distance < lowest)
                lowest = distance;
        }

        return bonus + lowest;
    }

    public float rateSplitDesire(Vector3fc wantedMin, Vector3fc wantedMax) {
        if (isLeaf()) {
            return this.rateSplitDesireLocal(wantedMin, wantedMax);
        } else {
            float lowest = Float.MAX_VALUE;
            for (var source : sources) {
                var value = source.rateSplitDesire(wantedMin, wantedMax);
                if (value < lowest)
                    lowest = value;
            }

            if (clonedFlag) {
                float local = this.rateSplitDesireLocal(wantedMin, wantedMax);
                if (local < lowest)
                    return local;
            } else if (this.splitAxis != null) {
                float local = this.rateSplitDesireLocal(wantedMin, wantedMax);
                if (local < lowest)
                    return local;
            }

            return lowest;
        }
    }

    public Vector3f getCenterOfMass() {
        return this.getMax().add(this.getMin()).mul(0.5f);
    }

    public Vector3f getMax() {
        if (isLeaf()) {
            return source.getMax();
        } else {
            Vector3f max = new Vector3f(-Float.MAX_VALUE);
            for (var source : sources)
                max.max(source.getMax());
            return max;
        }
    }

    public Vector3f getMin() {
        if (isLeaf()) {
            return source.getMin();
        } else {
            Vector3f min = new Vector3f(Float.MAX_VALUE);
            for (var source : sources)
                min.max(source.getMin());
            return min;
        }
    }

    public float getMass() {
        if (isLeaf()) {
            var max = source.getMax();
            var min = source.getMin();
            max.sub(min);
            return max.x * max.y * max.z;
        } else if (clonedFlag) {
            return sources.get(0).getMass();
        } else {
            float ttl = 0.0f;
            for (var source : sources)
                ttl += source.getMass();
            return ttl;
        }
    }

    public float getMass(Direction.Axis axis) {
        if (isLeaf()) {
            var max = source.getMax();
            var min = source.getMin();
            max.sub(min);
            return (float)axis.choose(max.x, max.y, max.z);
        } else if (clonedFlag) {
            return sources.get(0).getMass(axis);
        } else {
            float ttl = 0.0f;
            for (var source : sources)
                ttl += source.getMass(axis);
            return ttl;
        }
    }

    public float getWantedMass() {
        if (isLeaf()) {
            var size = new Vector3f();
            originalWantedMax.sub(originalWantedMin, size);
            return size.x * size.y * size.z;
        } else {
            float ttl = 0.0f;
            for (var source : sources)
                ttl += source.getWantedMass();
            return ttl;
        }
    }
}
