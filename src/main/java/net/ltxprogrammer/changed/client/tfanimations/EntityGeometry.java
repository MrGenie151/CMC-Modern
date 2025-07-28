package net.ltxprogrammer.changed.client.tfanimations;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.ltxprogrammer.changed.client.CubeExtender;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.core.Direction;
import org.joml.*;

import javax.annotation.Nullable;
import java.lang.Math;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class EntityGeometry {
    public float x;
    public float y;
    public float z;
    public float xRot;
    public float yRot;
    public float zRot;
    public boolean visible = true;
    public final ObjectArrayList<Cube> cubes = new ObjectArrayList<>();
    public final Object2ObjectArrayMap<String, EntityGeometry> children = new Object2ObjectArrayMap<>();

    public EntityGeometry(List<Cube> cubes, Map<String, EntityGeometry> children) {
        this.cubes.addAll(cubes);
        this.children.putAll(children);
    }

    public EntityGeometry(EntityGeometry copyFrom) {
        this(copyFrom, null);
    }

    public EntityGeometry(EntityGeometry copyFrom, @Nullable Predicate<EntityGeometry> includeChild) {
        this.x = copyFrom.x;
        this.y = copyFrom.y;
        this.z = copyFrom.z;
        this.xRot = copyFrom.xRot;
        this.yRot = copyFrom.yRot;
        this.zRot = copyFrom.zRot;
        this.visible = copyFrom.visible;

        copyFrom.cubes.forEach(cube -> this.cubes.add(new Cube(cube)));
        copyFrom.children.forEach((name, modelPart) -> {
            if (includeChild == null || includeChild.test(modelPart))
                this.children.put(name, new EntityGeometry(modelPart, includeChild));
        });
    }

    public EntityGeometry(ModelPart copyFrom) {
        this(copyFrom, null);
    }

    public EntityGeometry(ModelPart copyFrom, @Nullable Predicate<ModelPart> includeChild) {
        this.x = copyFrom.x;
        this.y = copyFrom.y;
        this.z = copyFrom.z;
        this.xRot = copyFrom.xRot;
        this.yRot = copyFrom.yRot;
        this.zRot = copyFrom.zRot;

        copyFrom.cubes.forEach(cube -> this.cubes.add(new Cube(cube)));
        copyFrom.children.forEach((name, modelPart) -> {
            if (includeChild == null || includeChild.test(modelPart))
                this.children.put(name, new EntityGeometry(modelPart, includeChild));
        });
    }

    public void visit(Consumer<? super EntityGeometry> visitor) {
        visitor.accept(this);
        children.values().forEach(visitor);
    }

    public PartPose storePose() {
        return PartPose.offsetAndRotation(x, y, z, xRot, yRot, zRot);
    }

    public void loadPose(PartPose pose) {
        this.x = pose.x;
        this.y = pose.y;
        this.z = pose.z;
        this.xRot = pose.xRot;
        this.yRot = pose.yRot;
        this.zRot = pose.zRot;
    }

    public void copyPoseFrom(EntityGeometry part) {
        this.xRot = part.xRot;
        this.yRot = part.yRot;
        this.zRot = part.zRot;
        this.x = part.x;
        this.y = part.y;
        this.z = part.z;
    }

    public void copyPoseFrom(ModelPart part) {
        this.xRot = part.xRot;
        this.yRot = part.yRot;
        this.zRot = part.zRot;
        this.x = part.x;
        this.y = part.y;
        this.z = part.z;
    }

    public boolean hasChild(String name) {
        return this.children.containsKey(name);
    }

    public EntityGeometry getChild(String name) {
        EntityGeometry part = this.children.get(name);
        if (part == null) {
            throw new NoSuchElementException("Can't find part " + name);
        } else {
            return part;
        }
    }

    public void setPos(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setRotation(float xRot, float yRot, float zRot) {
        this.xRot = xRot;
        this.yRot = yRot;
        this.zRot = zRot;
    }

    public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int overlay, int lightCoords) {
        this.render(poseStack, vertexConsumer, overlay, lightCoords, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    public void render(PoseStack poseStack, VertexConsumer vertexConsumer, int overlay, int lightCoords, float red, float green, float blue, float alpha) {
        if (this.visible) {
            if (!this.cubes.isEmpty() || !this.children.isEmpty()) {
                poseStack.pushPose();
                this.translateAndRotate(poseStack);
                this.compile(poseStack.last(), vertexConsumer, overlay, lightCoords, red, green, blue, alpha);

                for(EntityGeometry part : this.children.values()) {
                    part.render(poseStack, vertexConsumer, overlay, lightCoords, red, green, blue, alpha);
                }

                poseStack.popPose();
            }
        }
    }

    public void translateAndRotate(PoseStack poseStack) {
        poseStack.translate(this.x / 16.0F, this.y / 16.0F, this.z / 16.0F);
        if (this.xRot != 0.0F || this.yRot != 0.0F || this.zRot != 0.0F) {
            poseStack.mulPose((new Quaternionf()).rotationZYX(this.zRot, this.yRot, this.xRot));
        }
    }

    private void compile(PoseStack.Pose pose, VertexConsumer vertexConsumer, int overlay, int lightCoords, float red, float green, float blue, float alpha) {
        for(Cube cube : this.cubes) {
            cube.compile(pose, vertexConsumer, overlay, lightCoords, red, green, blue, alpha);
        }

    }

    public EntityGeometry offsetSize(float x, float y, float z) {
        visit(part -> {
            for (var cube : part.cubes)
                cube.offsetSize(x, y, z);
        });
        return this;
    }

    public static class Vertex {
        public final Vector3f pos;
        public float u;
        public float v;

        public Vertex(Vertex copyFrom) {
            this.pos = new Vector3f(copyFrom.pos);
            this.u = copyFrom.u;
            this.v = copyFrom.v;
        }

        public Vertex(ModelPart.Vertex copyFrom) {
            this.pos = new Vector3f(copyFrom.pos);
            this.u = copyFrom.u;
            this.v = copyFrom.v;
        }

        public Vertex(float x, float y, float z, float u, float v) {
            this(new Vector3f(x, y, z), u, v);
        }

        public Vertex remap(float u, float v) {
            return new Vertex(this.pos, u, v);
        }

        public Vertex(Vector3f pos, float u, float v) {
            this.pos = pos;
            this.u = u;
            this.v = v;
        }
    }

    public static class Polygon {
        public Vertex[] vertices;
        public Vector3f normal;

        public Polygon(Polygon copyFrom) {
            this.vertices = new Vertex[copyFrom.vertices.length];
            int i = 0;
            for (var poly : copyFrom.vertices) {
                this.vertices[i++] = new Vertex(poly);
            }
            this.normal = new Vector3f(copyFrom.normal);
        }

        public Polygon(ModelPart.Polygon copyFrom) {
            this.vertices = new Vertex[copyFrom.vertices.length];
            int i = 0;
            for (var poly : copyFrom.vertices) {
                this.vertices[i++] = new Vertex(poly);
            }
            this.normal = new Vector3f(copyFrom.normal);
        }
    }

    public static class Cube {
        public Polygon[] polygons;

        public Cube(Cube copyFrom) {
            final var copyPolys = copyFrom.polygons;
            this.polygons = new Polygon[copyPolys.length];
            int i = 0;
            for (var poly : copyPolys) {
                this.polygons[i++] = new Polygon(poly);
            }
        }

        public Cube(ModelPart.Cube copyFrom) {
            final var copyPolys = ((CubeExtender)copyFrom).getPolygons();
            this.polygons = new Polygon[copyPolys.length];
            int i = 0;
            for (var poly : copyPolys) {
                this.polygons[i++] = new Polygon(poly);
            }
        }

        @Nullable
        public Polygon getFace(Direction normal) {
            for (Polygon poly : polygons) {
                if (poly.normal.dot(normal.step()) >= 0.95f)
                    return poly;
            }
            return null;
        }

        public Cube clampToFit(Cube clampBy) {
            return clampToFit(clampBy, this);
        }

        public Cube clampToFit(Cube clampBy, Cube dest) {
            Vector3f sizeClamp = clampBy.getMax().sub(clampBy.getMin());
            Vector3f thisMin = this.getMin();
            Vector3f thisMax = this.getMax();
            Vector3f thisSize = new Vector3f();
            Vector3f thisCenter = new Vector3f();

            thisMax.sub(thisMin, thisSize);
            thisSize.add(thisMin, thisCenter);
            thisCenter.mul(0.5f);

            Vector3f deltaSize = new Vector3f();
            sizeClamp.sub(thisSize, deltaSize);
            deltaSize.set(Math.min(deltaSize.x, 0.0f), Math.min(deltaSize.y, 0.0f), Math.min(deltaSize.z, 0.0f));
            Vector3f scalarSize = new Vector3f();
            sizeClamp.div(thisSize, scalarSize);
            scalarSize.set(Math.min(scalarSize.x, 1.0f), Math.min(scalarSize.y, 1.0f), Math.min(scalarSize.z, 1.0f));

            for (Direction normal : Direction.values()) {
                Polygon thisFace = this.getFace(normal);
                Polygon destFace = dest.getFace(normal);
                if (thisFace == null || destFace == null)
                    continue;

                float minU = Float.MAX_VALUE;
                float maxU = -Float.MAX_VALUE;
                float minV = Float.MAX_VALUE;
                float maxV = -Float.MAX_VALUE;
                for (int i = 0; i < thisFace.vertices.length; ++i) {
                    if (thisFace.vertices[i].u < minU)
                        minU = thisFace.vertices[i].u;
                    if (thisFace.vertices[i].v < minV)
                        minV = thisFace.vertices[i].v;

                    if (thisFace.vertices[i].u > maxU)
                        maxU = thisFace.vertices[i].u;
                    if (thisFace.vertices[i].v > maxV)
                        maxV = thisFace.vertices[i].v;
                }

                float avgU = (minU + maxU) * 0.5f;
                float avgV = (minV + maxV) * 0.5f;

                for (int i = 0; i < thisFace.vertices.length; ++i) {
                    destFace.vertices[i].pos.x = thisCenter.x + ((thisFace.vertices[i].pos.x - thisCenter.x) * scalarSize.x);
                    destFace.vertices[i].pos.y = thisCenter.y + ((thisFace.vertices[i].pos.y - thisCenter.y) * scalarSize.y);
                    destFace.vertices[i].pos.z = thisCenter.z + ((thisFace.vertices[i].pos.z - thisCenter.z) * scalarSize.z);

                    float su;
                    float sv;
                    switch (normal) {
                        case NORTH, SOUTH -> { su = scalarSize.x; sv = scalarSize.y; }
                        case EAST, WEST -> { su = scalarSize.z; sv = scalarSize.y; }
                        case UP, DOWN -> { su = scalarSize.x; sv = scalarSize.z; }
                        default -> { su = 0.0f; sv = 0.0f; }
                    }

                    destFace.vertices[i].u = avgU + ((thisFace.vertices[i].u - avgU) * su);
                    destFace.vertices[i].v = avgV + ((thisFace.vertices[i].v - avgV) * sv);
                }
            }

            return dest;
        }

        public Cube scale(float x, float y, float z) {
            return scale(x, y, z, this);
        }

        public Cube scale(float x, float y, float z, Cube dest) {
            for (Direction normal : Direction.values()) {
                Polygon thisFace = this.getFace(normal);
                Polygon destFace = dest.getFace(normal);
                if (thisFace == null || destFace == null)
                    continue;

                for (int i = 0; i < thisFace.vertices.length; ++i) {
                    destFace.vertices[i].pos.x = thisFace.vertices[i].pos.x * x;
                    destFace.vertices[i].pos.y = thisFace.vertices[i].pos.y * y;
                    destFace.vertices[i].pos.z = thisFace.vertices[i].pos.z * z;
                }
            }

            return dest;
        }

        public Cube offsetSize(float x, float y, float z) {
            return offsetSize(x, y, z, this);
        }

        public Cube offsetSize(float x, float y, float z, Cube dest) {
            Vector3f thisMin = this.getMin();
            Vector3f thisMax = this.getMax();
            Vector3f thisSize = new Vector3f();
            Vector3f thisCenter = new Vector3f();

            thisMax.sub(thisMin, thisSize);
            thisSize.add(thisMin, thisCenter);
            thisCenter.mul(0.5f);

            for (Direction normal : Direction.values()) {
                Polygon thisFace = this.getFace(normal);
                Polygon destFace = dest.getFace(normal);
                if (thisFace == null || destFace == null)
                    continue;

                for (int i = 0; i < thisFace.vertices.length; ++i) {
                    destFace.vertices[i].pos.x = thisFace.vertices[i].pos.x + (thisFace.vertices[i].pos.x < thisCenter.x ? -x : x);
                    destFace.vertices[i].pos.y = thisFace.vertices[i].pos.y + (thisFace.vertices[i].pos.y < thisCenter.y ? -y : y);
                    destFace.vertices[i].pos.z = thisFace.vertices[i].pos.z + (thisFace.vertices[i].pos.z < thisCenter.z ? -z : z);
                }
            }

            return dest;
        }

        public Vector3f getMin() {
            float minX = Float.MAX_VALUE;
            float minY = Float.MAX_VALUE;
            float minZ = Float.MAX_VALUE;
            for (var poly : polygons) {
                for (var vert : poly.vertices) {
                    if (vert.pos.x > minX)
                        minX = vert.pos.x;
                    if (vert.pos.y > minY)
                        minY = vert.pos.y;
                    if (vert.pos.z > minZ)
                        minZ = vert.pos.z;
                }
            }

            return new Vector3f(minX, minY, minZ);
        }

        public Vector3f getMax() {
            float maxX = -Float.MAX_VALUE;
            float maxY = -Float.MAX_VALUE;
            float maxZ = -Float.MAX_VALUE;
            for (var poly : polygons) {
                for (var vert : poly.vertices) {
                    if (vert.pos.x < maxX)
                        maxX = vert.pos.x;
                    if (vert.pos.y < maxY)
                        maxY = vert.pos.y;
                    if (vert.pos.z < maxZ)
                        maxZ = vert.pos.z;
                }
            }

            return new Vector3f(maxX, maxY, maxZ);
        }

        public void compile(PoseStack.Pose pose, VertexConsumer vertexConsumer, int overlay, int lightCoords, float red, float green, float blue, float alpha) {
            Matrix4f matrix4f = pose.pose();
            Matrix3f matrix3f = pose.normal();

            for(Polygon polygon : this.polygons) {
                Vector3f orientedNormal = matrix3f.transform(new Vector3f(polygon.normal));
                float nx = orientedNormal.x();
                float ny = orientedNormal.y();
                float nz = orientedNormal.z();

                for(Vertex vertex : polygon.vertices) {
                    float x = vertex.pos.x() / 16.0F;
                    float y = vertex.pos.y() / 16.0F;
                    float z = vertex.pos.z() / 16.0F;
                    Vector4f vector4f = matrix4f.transform(new Vector4f(x, y, z, 1.0F));
                    vertexConsumer.vertex(vector4f.x(), vector4f.y(), vector4f.z(), red, green, blue, alpha, vertex.u, vertex.v, lightCoords, overlay, nx, ny, nz);
                }
            }
        }
    }
}
