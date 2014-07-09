/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pocketviewer.Renderer;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

import pocketviewer.Objects.Chunk;
import pocketviewer.Objects.ChunkManager;
import pocketviewer.Objects.World;

import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

/**
 *
 * @author Jocopa3
 */
public class ChunkRenderer {
	
	public World world;
	public ChunkManager chunks;
	
	public WorldRenderer worldRenderer;
	BlockRenderer blockRenderer;
	
	public int[][][] buffers;
	public int[][][] bufferLengths;
	
	FloatBuffer vertexArray;
	
	public ChunkRenderer(WorldRenderer worldRenderer){
		this.worldRenderer = worldRenderer;
		world = worldRenderer.world;
		
		blockRenderer = new BlockRenderer();
		chunks = world.chunks;
		buffers = new int[chunks.length][chunks.width][VBOHelper.DEFAULT_BUFFER_AMOUNT];
		bufferLengths = new int[chunks.length][chunks.width][VBOHelper.DEFAULT_BUFFER_AMOUNT];
	}
	
	public void renderAllChunks(){
		for(int x = 0; x < chunks.width; x++){
			for(int z = 0; z < chunks.length; z++){
				renderChunk(x,z);
			}
		}
	}

	public void renderChunk(int x, int z){
		if(x < 0 || z < 0 || x >= chunks.width || z >= chunks.length)
			return;

		int[] chunkBuffer = getChunkBuffer(x, z);
		int[] chunkBufferLengths = getChunkBufferLength(x, z);
		Chunk c = chunks.getChunk(x, z);

		if(c.needsUpdate || chunkBuffer == null)
			chunks.setChunk(x, z, updateChunkVBO(c, x, z)); //Update and set the chunk as updated

		glPushMatrix();
		glTranslatef(x << 4, 0, z << 4);
		
		for(int i = 0; i < 1; i++){
			if(chunkBuffer[i]<=0||chunkBufferLengths[i]<=0)
				continue;
			
			glBindBuffer(GL_ARRAY_BUFFER, chunkBuffer[i]);

			glVertexPointer(3, GL_FLOAT, 32, 0); // float at index 0
			glColorPointer(3, GL_FLOAT, 32, 12);
			//glTexCoordPointer(2, GL_FLOAT, 32, 8);

			glEnableClientState(GL_VERTEX_ARRAY);
			glEnableClientState(GL_COLOR_ARRAY);
			//glEnableClientState(GL_TEXTURE_COORD_ARRAY);
			
			//System.out.println(chunkBufferLengths[i]+" "+chunkBuffer[i]); //Debug
			
			//glDrawArrays(GL_QUADS, 0, chunkBufferLengths[i]);

			glBindBuffer(GL_ARRAY_BUFFER, 0);

			//glDisableClientState(GL_TEXTURE_COORD_ARRAY);
			glDisableClientState(GL_COLOR_ARRAY);
			glDisableClientState(GL_VERTEX_ARRAY);
			
			glBindBuffer(GL_ARRAY_BUFFER, 0);
		}

		glPopMatrix();
	}

	//Do NOT update the VBO unless a block is placed/removed/updated!
	//Updates the VBO for a given chunk and returns the updated chunk
	public Chunk updateChunkVBO(Chunk chunk, int cx, int cz){
		long g = System.currentTimeMillis(); //Measure performance
		
		VBOHelper vboHelper = new VBOHelper(); //Create a new VBOHelper
		vboHelper.start(); //Start the VBOHelper
		
		//Occlusion culler; needs eventual fixing to check if block is solid/opaque
		for(int x = 0; x < chunk.width; x++){
			for(int y = 0; y < chunk.height; y++){
				for(int z = 0; z < chunk.length; z++){
					if(world.getBlockID(x, y, z) > 0 || (x==1&&y==1&&z==1)){ //Remove second statement; used for debug
						if(world.getBlockID(cx*16 + x-1, y, cz*16 + z) == 0){ //Check right
							Vertex[] face = blockRenderer.getRight(x, y, z);
							vboHelper.addVertices(face);
						}
						if(world.getBlockID(cx*16 + x+1, y, cz*16 + z) == 0){ //Check left
							Vertex[] face = blockRenderer.getLeft(x, y, z);
							vboHelper.addVertices(face);
						}
						if(world.getBlockID(cx*16 + x, y-1, cz*16 + z) == 0){ //Check bottom
							Vertex[] face = blockRenderer.getBottom(x, y, z);
							vboHelper.addVertices(face);
						}
						if(world.getBlockID(cx*16 + x, y+1, cz*16 + z) == 0){ //Check top
							Vertex[] face = blockRenderer.getTop(x, y, z);
							vboHelper.addVertices(face);
						}
						if(world.getBlockID(cx*16 + x, y, cz*16 + z-1) == 0){ //Check front
							Vertex[] face = blockRenderer.getFront(x, y, z);
							vboHelper.addVertices(face);
						}
						if(world.getBlockID(cx*16 + x, y, cz*16 + z+1) == 0){ //Check back
							Vertex[] face = blockRenderer.getBack(x, y, z);
							vboHelper.addVertices(face);
						}
					}
				}
			}
		}
		
		Vertex[] face = BlockRenderer.getBack(2,2,2); //Debug
		vboHelper.addVertices(face);
		
		vboHelper.stop(); //End the VBOHelper
		
		buffers[cx][cz] = vboHelper.getBufferHandlers(); //Get VBO buffer handles
		bufferLengths[cx][cz] = vboHelper.getBufferLengths(); //Get VBO element lengths
		
		System.out.println("Buffer ID: "+buffers[cx][cz][0]+" Element Length: "+bufferLengths[cx][cz][0]); //Debug buffer and length
		
		chunk.needsUpdate = false; //Prevents updating the VBO again until necessary
		System.out.println("Chunk VBO time: " + (System.currentTimeMillis()-g) + "ms"); //Debug
		
		return chunk; //Return the updated chunk back to the caller
	}
	
	//Deletes all buffers for all chunks
	public void deleteAllChunkBuffers(){
		for(int x = 0; x < chunks.length; x++){
			for(int z = 0; z < chunks.width; z++){
				deleteChunkBuffer(x, z);
			}
		}
	}
	
	//Deletes all chunk buffers and clears stored buffers for a given chunk
	public void deleteChunkBuffer(int x, int z){
		int[] chunkBuffers = getChunkBuffer(x, z);
		for(int i : chunkBuffers){
			glDeleteBuffers(i);
		}
		bufferLengths[x][z] = null;
		buffers[x][z] = null;
	}
	
	//Returns int array of buffer handlers for a given chunk
	public int[] getChunkBuffer(int x, int z){
		return buffers[x][z];
	}
	
	//Returns int array of buffer lenghts for a given chunk
	public int[] getChunkBufferLength(int x, int z){
		return bufferLengths[x][z];
	}
}
