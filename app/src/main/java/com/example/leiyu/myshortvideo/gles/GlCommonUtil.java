package com.example.leiyu.myshortvideo.gles;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.opengl.EGL14;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

@TargetApi(18)
public class GlCommonUtil {
	private static final String TAG = "GlCommonUtil";

	public static int createProgram(String vertexSource, String fragmentSource) {
		int vs = loadShader(GLES20.GL_VERTEX_SHADER,   vertexSource);
		int fs = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
		int program = GLES20.glCreateProgram();
		GLES20.glAttachShader(program, vs);
		GLES20.glAttachShader(program, fs);
		GLES20.glLinkProgram(program);
		int[] linkStatus = new int[1];
		GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
		if (linkStatus[0] != GLES20.GL_TRUE) {
			Log.e(TAG, "Could not link program:");
			Log.e(TAG, GLES20.glGetProgramInfoLog(program));
			GLES20.glDeleteProgram(program);
			program = 0;
		}
		return program;
	}

	public static int loadShader(int shaderType, String source) {
		int shader = GLES20.glCreateShader(shaderType);
		GLES20.glShaderSource(shader, source);
		GLES20.glCompileShader(shader);
		//
		int[] compiled = new int[1];
		GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
		if (compiled[0] == 0) {
			Log.e(TAG, "Could not compile shader(TYPE=" + shaderType + "):");
			Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
			GLES20.glDeleteShader(shader);
			shader = 0;
		}
		//
		return shader;
	}

	public static void checkGlError(String op) {
		int error;
		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
			Log.e(TAG, op + ": glGetError: 0x" + Integer.toHexString(error));
			throw new RuntimeException("glGetError encountered (see log)");
		}
	}

	public static void checkEglError(String op) {
		int error;
		while ((error = EGL14.eglGetError()) != EGL14.EGL_SUCCESS) {
			Log.e(TAG, op + ": eglGetError: 0x" + Integer.toHexString(error));
			throw new RuntimeException("eglGetError encountered (see log)");
		}
	}

	public static void deleteGLTexture(int iTextureID) {
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		int[] aTextures = new int[]{iTextureID};
		GLES20.glDeleteTextures(1, aTextures, 0);
	}

	public static int createTexture2d(Bitmap bitmap) {
		int[] textures = new int[1];
		checkGlError("glBindTexture mTextureID");
		GLES20.glGenTextures(1, textures, 0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
				GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
				GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
				GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
				GLES20.GL_CLAMP_TO_EDGE);
		checkGlError("glTexParameter");
		return textures[0];
	}

	public static int createTextureEos() {
		int[] textures = new int[1];
		checkGlError("glBindTexture mTextureID");
		GLES20.glGenTextures(1, textures, 0);
		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);

		GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
				GLES20.GL_NEAREST);
		GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
				GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S,
				GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T,
				GLES20.GL_CLAMP_TO_EDGE);
		checkGlError("glTexParameter");
		return textures[0];
	}

	public static int createTexture2d() {
		int[] texId  = new int[]{0};
		GLES20.glGenTextures(1, texId, 0);

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId[0]);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
				GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
				GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
				GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
				GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
		return texId[0];
	}

}
