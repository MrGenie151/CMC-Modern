package net.ltxprogrammer.changed.mixin.render;

import com.mojang.datafixers.util.Pair;
import net.ltxprogrammer.changed.Changed;
import net.ltxprogrammer.changed.client.CubeExtender;
import net.ltxprogrammer.changed.util.Cacheable;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Arrays;
import java.util.Set;

@Mixin(ModelPart.Cube.class)
public abstract class CubeMixin implements CubeExtender {
    // TODO: This is no longer guaranteed to have 6 faces
    @Shadow @Final private ModelPart.Polygon[] polygons;
    @Unique private Cacheable<Vector3f> visualMin = Cacheable.of(() -> {
        Vector3f min = null;
        for (var polygon : this.polygons) {
            for (var vertex : polygon.vertices) {
                if (min == null) {
                    min = new Vector3f(vertex.pos);
                    continue;
                }

                min.x = Math.min(min.x, vertex.pos.x);
                min.y = Math.min(min.y, vertex.pos.y);
                min.z = Math.min(min.z, vertex.pos.z);
            }
        }
        return min;
    });
    @Unique private Cacheable<Vector3f> visualMax = Cacheable.of(() -> {
        Vector3f max = null;
        for (var polygon : this.polygons) {
            for (var vertex : polygon.vertices) {
                if (max == null) {
                    max = new Vector3f(vertex.pos);
                    continue;
                }

                max.x = Math.max(max.x, vertex.pos.x);
                max.y = Math.max(max.y, vertex.pos.y);
                max.z = Math.max(max.z, vertex.pos.z);
            }
        }
        return max;
    });

    @Override
    public Vector3f getVisualMin() {
        return visualMin.get();
    }

    @Override
    public Vector3f getVisualMax() {
        return visualMax.get();
    }

    @Override
    public UVPair getUV(Vector3f cubeSurfaceNormal) {
        ModelPart.Polygon surface = null;
        float bestMatch = -1.0f;

        float xLerp = 0.0f;
        float yLerp = 0.0f;

        for (ModelPart.Polygon polygon : this.polygons) {
            var polyNormal = polygon.normal;

            float thisMatch = polyNormal.dot(cubeSurfaceNormal);
            if (thisMatch > bestMatch) {
                surface = polygon;
                bestMatch = thisMatch;

                if (Mth.abs(polyNormal.x()) > Mth.abs(polyNormal.y()) && Mth.abs(polyNormal.x()) > Mth.abs(polyNormal.z())) {
                    xLerp = cubeSurfaceNormal.z() * 0.5f + 0.5f;
                    yLerp = cubeSurfaceNormal.y() * 0.5f + 0.5f;
                } else if (Mth.abs(polyNormal.y()) > Mth.abs(polyNormal.x()) && Mth.abs(polyNormal.y()) > Mth.abs(polyNormal.z())) {
                    xLerp = cubeSurfaceNormal.x() * 0.5f + 0.5f;
                    yLerp = cubeSurfaceNormal.z() * 0.5f + 0.5f;
                } else {
                    xLerp = cubeSurfaceNormal.x() * 0.5f + 0.5f;
                    yLerp = cubeSurfaceNormal.y() * 0.5f + 0.5f;
                }
            }
        }

        if (surface == null) {
            Changed.LOGGER.warn("Null surface encountered for given normal {}, with {} polygons", cubeSurfaceNormal, this.polygons.length);
            return new UVPair(0, 0);
        }

        float uX = Mth.lerp(xLerp, surface.vertices[0].u, surface.vertices[1].u);
        float uY = Mth.lerp(xLerp, surface.vertices[3].u, surface.vertices[2].u);
        float vX = Mth.lerp(xLerp, surface.vertices[0].v, surface.vertices[1].v);
        float vY = Mth.lerp(xLerp, surface.vertices[3].v, surface.vertices[2].v);

        return new UVPair(Mth.lerp(yLerp, uX, uY), Mth.lerp(yLerp, vX, vY));
    }

    @Unique
    private static final ModelPart.Vertex NULL_VERTEX = new ModelPart.Vertex(0, 0, 0, 0, 0);

    @Unique
    private ModelPart.Polygon getFaceFromDirection(Direction dir) {
        Vector3f step = dir.getAxis() == Direction.Axis.Y ? dir.getOpposite().step() : dir.step();
        for (ModelPart.Polygon polygon : polygons) {
            if (polygon.normal.dot(step) >= 0.95f)
                return polygon;
        }
        return null;
    }

    @Override
    public void removeSides(Set<Direction> directions) {
        for (var dir : directions) {
            var polygon = getFaceFromDirection(dir);
            if (polygon != null)
                Arrays.fill(polygon.vertices, NULL_VERTEX);
        }
    }

    @Override
    public void copyUVStarts(Set<Pair<Direction, Direction>> directions) {
        for (var copy : directions) {
            Direction fromDir = copy.getFirst();
            Direction toDir = copy.getSecond();

            ModelPart.Polygon from = getFaceFromDirection(fromDir);
            ModelPart.Polygon to = getFaceFromDirection(toDir);

            if (from == to || from == null || to == null)
                continue;

            for (int i = 0; i < from.vertices.length && i < to.vertices.length; ++i) {
                var fromVtx = from.vertices[i];
                var toVtx = to.vertices[i];

                to.vertices[i] = new ModelPart.Vertex(toVtx.pos, fromVtx.u, fromVtx.v);
            }
        }
    }

    @Override
    public ModelPart.Polygon[] getPolygons() {
        return polygons;
    }

    @Override
    public void copyPolygonsFrom(ModelPart.Cube cube) {
        ModelPart.Polygon[] otherPoly = ((CubeExtender)cube).getPolygons();
        for (int i = 0; i < otherPoly.length; ++i) {
            ModelPart.Vertex[] nVertices = new ModelPart.Vertex[] {
                    otherPoly[i].vertices[0],
                    otherPoly[i].vertices[1],
                    otherPoly[i].vertices[2],
                    otherPoly[i].vertices[3]
            };

            this.polygons[i] = new ModelPart.Polygon(nVertices, 0.0f, 0.0f, 0.0f, 0.0f,
                    1.0f, 1.0f, false, Direction.getNearest(otherPoly[i].normal.x(), otherPoly[i].normal.y(), otherPoly[i].normal.z()));

            for (int v = 0; v < this.polygons[i].vertices.length; ++v) {
                final ModelPart.Vertex otherVert = otherPoly[i].vertices[v];

                // Deep copy
                this.polygons[i].vertices[v] = otherVert.remap(otherVert.u, otherVert.v);
            }
        }
    }

    @Unique
    private void extendVertex(ModelPart.Polygon poly, int vertex, float x, float y, float z) {
        poly.vertices[vertex] = new ModelPart.Vertex(
                poly.vertices[vertex].pos.x() + x,
                poly.vertices[vertex].pos.y() + y,
                poly.vertices[vertex].pos.z() + z,
                poly.vertices[vertex].u, poly.vertices[vertex].v);
    }

    @Override
    public void extendCube(float x, float y, float z) {
        extendVertex(this.polygons[0], 0, x, -y, z);
        extendVertex(this.polygons[0], 1, x, -y, -z);
        extendVertex(this.polygons[0], 2, x, y, -z);
        extendVertex(this.polygons[0], 3, x, y, z);
        
        extendVertex(this.polygons[1], 0, -x, -y, -z);
        extendVertex(this.polygons[1], 1, -x, -y, z);
        extendVertex(this.polygons[1], 2, -x, y, z);
        extendVertex(this.polygons[1], 3, -x, y, -z);
        
        extendVertex(this.polygons[2], 0, x, -y, z);
        extendVertex(this.polygons[2], 1, -x, -y, z);
        extendVertex(this.polygons[2], 2, -x, -y, -z);
        extendVertex(this.polygons[2], 3, x, -y, -z);
        
        extendVertex(this.polygons[3], 0, x, y, -z);
        extendVertex(this.polygons[3], 1, -x, y, -z);
        extendVertex(this.polygons[3], 2, -x, y, z);
        extendVertex(this.polygons[3], 3, x, y, z);

        extendVertex(this.polygons[4], 0, x, -y, -z);
        extendVertex(this.polygons[4], 1, -x, -y, -z);
        extendVertex(this.polygons[4], 2, -x, y, -z);
        extendVertex(this.polygons[4], 3, x, y, -z);

        extendVertex(this.polygons[5], 0, -x, -y, z);
        extendVertex(this.polygons[5], 1, x, -y, z);
        extendVertex(this.polygons[5], 2, x, y, z);
        extendVertex(this.polygons[5], 3, -x, y, z);
    }

    @Override
    public ModelPart.Polygon getRandomPolygonWeighted(RandomSource random) {
        float[] weights = new float[polygons.length];
        float totalWeight = 0.0f;

        for (int i = 0; i < polygons.length; ++i) {
            var polygon = polygons[i];
            Vector3f min = null, max = null;

            for (var vertex : polygon.vertices) {
                if (min == null)
                    min = new Vector3f(vertex.pos);
                if (max == null)
                    max = new Vector3f(vertex.pos);

                min.x = Math.min(min.x, vertex.pos.x);
                min.y = Math.min(min.y, vertex.pos.y);
                min.z = Math.min(min.z, vertex.pos.z);
                max.x = Math.max(max.x, vertex.pos.x);
                max.y = Math.max(max.y, vertex.pos.y);
                max.z = Math.max(max.z, vertex.pos.z);
            }

            if (min == null) {
                weights[i] = 0.0f;
                continue;
            }

            if (max.x == min.x)
                weights[i] = Math.abs((max.y - min.y) * (max.z - min.z));
            else if (max.y == min.y)
                weights[i] = Math.abs((max.x - min.x) * (max.z - min.z));
            else
                weights[i] = Math.abs((max.x - min.x) * (max.y - min.y));
            totalWeight += weights[i];
        }

        ModelPart.Polygon polygon = null;
        float chosenPolygon = random.nextFloat() * totalWeight;
        for (int i = 0; i < polygons.length; ++i) {
            if (weights[i] <= 0.0f)
                continue;
            if ((chosenPolygon -= weights[i]) > 0.0f)
                continue;

            return polygons[i];
        }

        throw new IndexOutOfBoundsException();
    }
}
