package pocketviewer.Renderer;

import pocketviewer.Objects.Chunk;
import static pocketviewer.Utils.MathUtils.*;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import static org.lwjgl.opengl.GL11.glLoadIdentity;
/**
 * Only renders camera for now
 * @author Jocopa3
 */
public class PlayerRenderer {
	public Perspective camera;
	
	static float dx = 0.0f;
	static float dy = 0.0f;

	static float mouseSensitivity = 0.1f;
	static float movementSpeed = 0.75f;
    
    public WorldRenderer renderer;
	
	public PlayerRenderer(WorldRenderer world){
		camera = new Perspective(0,0,0);
        renderer = world;
	}
	
	public void updateCamera(){
		//distance in mouse movement from the last getDX() call.
		dx = Mouse.getDX();
		//distance in mouse movement from the last getDY() call.
		dy = Mouse.getDY();
 
		//controll camera yaw from x movement fromt the mouse
		camera.yaw(dx * mouseSensitivity);
		//controll camera pitch from y movement fromt the mouse
		camera.pitch(-dy * mouseSensitivity);
		
		if (Keyboard.isKeyDown(Keyboard.KEY_W)){
		    camera.moveForward(movementSpeed);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_S)){
		    camera.moveBackward(movementSpeed);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_A)){
			camera.moveLeft(movementSpeed);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_D)){
		    camera.moveRight(movementSpeed);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)){
		    camera.moveUp(movementSpeed);
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)){
		    camera.moveDown(movementSpeed);
		}
		
		glLoadIdentity();
        //look through the camera before you draw anything
        camera.lookThrough();
	}
	
	public boolean isChunkInRange(Chunk chunk){
        if(chunk == null)
            return false;
        
        float cx = (chunk.xPos << 4) + (chunk.width >> 1), cz = (chunk.zPos << 4) + (chunk.length >> 1); //Chunk center
		float px = -camera.getX(), pz = -camera.getZ();
        
        return distSqrd(cx, cz, px, pz) < sqr(renderer.renderDistance); //uses un-normalized distance formula for speed
	}
}
