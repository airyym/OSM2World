package org.osm2world.core.target.jogl;

import java.awt.Color;
import java.awt.Font;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import org.osm2world.core.math.TriangleXYZ;
import org.osm2world.core.math.Vector3D;
import org.osm2world.core.math.VectorXYZ;
import org.osm2world.core.math.VectorXZ;
import org.osm2world.core.target.common.Primitive;
import org.osm2world.core.target.common.PrimitiveTarget;
import org.osm2world.core.target.common.Primitive.Type;
import org.osm2world.core.target.common.material.Material;
import org.osm2world.core.target.common.material.Material.Lighting;
import org.osm2world.core.target.common.rendering.Camera;
import org.osm2world.core.target.common.rendering.Projection;

import com.sun.opengl.util.j2d.TextRenderer;

public class JOGLTarget extends PrimitiveTarget<RenderableToJOGL> {
	
	private final GL gl;
	private final Camera camera;
		
	public JOGLTarget(GL gl, Camera camera) {
		this.gl = gl;
		this.camera = camera;
	}
	
	@Override
	public Class<RenderableToJOGL> getRenderableType() {
		return RenderableToJOGL.class;
	}
	
	@Override
	public void render(RenderableToJOGL renderable) {
		renderable.renderTo(gl, camera);
	}

	@Override
	protected void drawPrimitive(Primitive.Type type, Material material,
			List<? extends VectorXYZ> vertices, VectorXYZ[] normals) {
		
		assert vertices.size() == normals.length;
		
		setMaterial(gl, material);
		
		gl.glBegin(getGLConstant(type));
        		
		for (int i = 0; i < vertices.size(); i++) {
			gl.glNormal3d(normals[i].x, normals[i].y, -normals[i].z);
	        gl.glVertex3d(vertices.get(i).x, vertices.get(i).y, -vertices.get(i).z);
			i ++;
		}
		
        gl.glEnd();
		
	}
	
	private void drawPrimitive(int primitive, Color color,
			List<? extends VectorXYZ> vs) {
		
		gl.glColor3f(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f);
		
		gl.glBegin(primitive);
        
		for (VectorXYZ v : vs) {
	        gl.glVertex3d(v.getX(), v.getY(), -v.getZ());
		}
		
        gl.glEnd();
        
	}
	
	public void drawPoints(Color color, VectorXYZ... vs) {
		drawPrimitive(GL.GL_POINTS, color, Arrays.asList(vs));
	}

	public void drawLineStrip(Color color, VectorXYZ... vs) {
		drawLineStrip(color, Arrays.asList(vs));
	}
	
	public void drawLineStrip(Color color, List<VectorXYZ> vs) {
		drawPrimitive(GL.GL_LINE_STRIP, color, vs);
	}
	
	public void drawLineStrip(Color color, int width, VectorXYZ... vs) {
		gl.glLineWidth(width);
		drawLineStrip(color, vs);
		gl.glLineWidth(1);
	}

	public void drawLineLoop(Color color, List<? extends VectorXYZ> vs) {
		drawPrimitive(GL.GL_LINE_LOOP, color, vs);
	}

	public void drawArrow(Color color, float headLength, VectorXYZ... vs) {
		
		drawLineStrip(color, vs);
		
		/* draw head */
		
		VectorXYZ lastV = VectorXYZ.xyz(vs[vs.length-1]);
		VectorXYZ slastV = VectorXYZ.xyz(vs[vs.length-2]);
		
		VectorXYZ endDir = lastV.subtract(slastV).normalize();
		VectorXYZ headStart = lastV.subtract(endDir.mult(headLength));
		
		VectorXZ endDirXZ = endDir.xz();
		if (endDirXZ.lengthSquared() < 0.01) { //(almost) vertical vector
			endDirXZ = VectorXZ.X_UNIT;
		} else {
			endDirXZ = endDirXZ.normalize();
		}
		VectorXZ endNormalXZ = endDirXZ.rightNormal();
				
		drawTriangleStrip(color,
				lastV,
				headStart.add(endDirXZ.mult(headLength/2)),
				headStart.subtract(endDirXZ.mult(headLength/2)));
        		
		drawTriangleStrip(color,
				lastV,
				headStart.add(endNormalXZ.mult(headLength/2)),
				headStart.subtract(endNormalXZ.mult(headLength/2)));
		
	}
	
	public void drawTriangleStrip(Color color, VectorXYZ... vs) {
		drawPrimitive(GL.GL_TRIANGLE_STRIP, color, Arrays.asList(vs));
	}
	
	public void drawTriangles(Color color, Collection<TriangleXYZ> triangles) {
		
		gl.glColor3f(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f);
		gl.glBegin(GL.GL_TRIANGLES);
        
		for (TriangleXYZ triangle : triangles) {
	        gl.glVertex3d(triangle.v1.x, triangle.v1.y, -triangle.v1.z);
	        gl.glVertex3d(triangle.v2.x, triangle.v2.y, -triangle.v2.z);
	        gl.glVertex3d(triangle.v3.x, triangle.v3.y, -triangle.v3.z);
		}

        gl.glEnd();
        
	}

	public void drawPolygon(Color color, VectorXYZ... vs) {
		drawPrimitive(GL.GL_POLYGON, color, Arrays.asList(vs));
	}
	
//	//TODO: own class for Texture, so Target classes can offer load texture
//	public void drawBillboard(VectorXYZ center, float halfWidth, float halfHeight,
//			Texture texture, Camera camera) {
//
//		VectorXYZ right = camera.getRight();
//		double rightXScaled = halfWidth*right.getX();
//		double rightZScaled = halfWidth*right.getZ();
//
//		TextureCoords tc = texture.getImageTexCoords();
//
//    	gl.glColor3f(1, 1, 1);
//
//		gl.glEnable(GL.GL_TEXTURE_2D);
//        gl.glEnable(GL.GL_BLEND);
//        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
//        gl.glEnable(GL.GL_ALPHA_TEST);
//        gl.glAlphaFunc(GL.GL_GREATER, 0);
//        //TODO: disable calls?
//
//        gl.glBegin(GL.GL_QUADS);
//
//		texture.bind();
//
//		gl.glTexCoord2f(tc.left(), tc.bottom());
//        gl.glVertex3d(
//        		center.getX() - rightXScaled,
//        		center.getY() - halfHeight,
//        		-(center.getZ() - rightZScaled));
//
//		gl.glTexCoord2f(tc.right(), tc.bottom());
//        gl.glVertex3d(
//        		center.getX() + rightXScaled,
//        		center.getY() - halfHeight,
//        		-(center.getZ() + rightZScaled));
//
//		gl.glTexCoord2f(tc.right(), tc.top());
//        gl.glVertex3d(
//        		center.getX() + rightXScaled,
//        		center.getY() + halfHeight,
//        		-(center.getZ() + rightZScaled));
//
//		gl.glTexCoord2f(tc.left(), tc.top());
//        gl.glVertex3d(
//        		center.getX() - rightXScaled,
//        		center.getY() + halfHeight,
//        		-(center.getZ() - rightZScaled));
//
//        gl.glDisable(GL.GL_TEXTURE_2D);
//
//	}
//
//	public static Texture loadTexture(String fileName) throws GLException, IOException
//	{
//	  File file = new File("resources" + File.separator + fileName);
//	  Texture result = null;
//
//	  result = TextureIO.newTexture(file, false);
//	  result.setTexParameteri(GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR); //TODO (performance): GL_NEAREST for performance?
//	  result.setTexParameteri(GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR); //TODO (performance): GL_NEAREST for performance?
//
//	  return result;
//	}

	private static final TextRenderer textRenderer = new TextRenderer(
			new Font("SansSerif", Font.PLAIN, 12), true, false);
	//needs quite a bit of memory, so it must not be created for each instance!
	
	public void drawText(String string, Vector3D pos, Color color) {
		textRenderer.setColor(color);
		textRenderer.begin3DRendering();
		textRenderer.draw3D(string, (float)pos.getX(), (float)pos.getY(), -(float)pos.getZ(), 0.05f);
	}

	public void drawText(String string, int x, int y,
			int screenWidth, int screenHeight, Color color) {
		textRenderer.beginRendering(screenWidth, screenHeight);
		textRenderer.setColor(color);
		textRenderer.draw(string, x, y);
		textRenderer.endRendering();
	}

	public static final void setCameraMatrices(GL gl, Camera camera) {
		VectorXYZ pos = camera.getPos();
		VectorXYZ lookAt = camera.getLookAt();
//		VectorXYZ dir = lookAt.subtract(pos);
//		VectorXYZ right = dir.cross(VectorXYZ.Y_UNIT).normalize();
//		VectorXYZ up = right.cross(dir);
		new GLU().gluLookAt(
				pos.x, pos.y, -pos.z,
				lookAt.x, lookAt.y, -lookAt.z,
				0, 1f, 0f);
	}
	
	public static final void setProjectionMatrices(GL gl, Projection projection) {
		setProjectionMatricesForPart(gl, projection, 0, 1, 0, 1);
	}

	/**
	 * similar to {@link #setProjectionMatrices(GL, Projection)},
	 * but allows rendering only a part of the "normal" image.
	 * For example, with xStart=0, xEnd=0.5, yStart=0 and yEnd=1,
	 * only the left half of the full image will be rendered,
	 * but it will be stretched to cover the available space.
	 * 
	 * Only supported for orthographic projections!
	 */
	public static final void setProjectionMatricesForPart(GL gl, Projection projection,
			double xStart, double xEnd, double yStart, double yEnd) {
		
		if ((xStart != 0 || xEnd != 1 || yStart != 0 || yEnd != 1)
				&& !projection.isOrthographic()) {
			throw new IllegalArgumentException("section rendering only supported "
					+ "for orthographic projections");
		}
		
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		
		if (projection.isOrthographic()) {

			double volumeWidth = projection.getAspectRatio() * projection.getVolumeHeight();
			
			gl.glOrtho(
					(-0.5 + xStart) * volumeWidth,
					(-0.5 + xEnd  ) * volumeWidth,
					(-0.5 + yStart) * projection.getVolumeHeight(),
					(-0.5 + yEnd  ) * projection.getVolumeHeight(),
					projection.getNearClippingDistance(),
					projection.getFarClippingDistance());
			
		} else { //perspective

			new GLU().gluPerspective(
					projection.getVertAngle(),
					projection.getAspectRatio(),
					projection.getNearClippingDistance(),
					projection.getFarClippingDistance());
			
		}

		gl.glMatrixMode(GL.GL_MODELVIEW);
		
	}

	public static final void setMaterial(GL gl, Material material) {
		
		if (material.getLighting() == Lighting.SMOOTH) {
			gl.glShadeModel(GL.GL_SMOOTH);
		} else {
			gl.glShadeModel(GL.GL_FLAT);
		}

		setFrontMaterialColor(gl, GL.GL_AMBIENT, material.ambientColor());
		setFrontMaterialColor(gl, GL.GL_DIFFUSE, material.diffuseColor());
		
	}

	public static final void setFrontMaterialColor(GL gl, int pname, Color color) {
		float ambientColor[] = {0, 0, 0, 1};
		color.getRGBColorComponents(ambientColor);
		gl.glMaterialfv(GL.GL_FRONT, pname, FloatBuffer.wrap(ambientColor));
	}

	public static final int getGLConstant(Type type) {
		switch (type) {
		case TRIANGLE_STRIP: return GL.GL_TRIANGLE_STRIP;
		case TRIANGLE_FAN: return GL.GL_TRIANGLE_FAN;
		case TRIANGLES: return GL.GL_TRIANGLES;
		case CONVEX_POLYGON: return GL.GL_POLYGON;
		default: throw new Error("programming error: unhandled primitive type");
		}
	}
	
}
