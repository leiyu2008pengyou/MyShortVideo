package com.example.leiyu.myshortvideo.utils;

/**
 * Created by leiyu on 2018/3/1.
 */

public class MyConstant {
    public static final String SHADER_NULL_VERTEX = "" +
            "attribute vec4 position;\n" +
            "attribute vec4 inputTextureCoordinate;\n" +
            "\n" +
            "uniform   mat4 uPosMtx;\n" +
            "uniform   mat4 uTexMtx;\n" +
            "varying   vec2 textureCoordinate;\n" +
            "void main() {\n" +
            "  gl_Position = uPosMtx * position;\n" +
            "  textureCoordinate   = (uTexMtx * inputTextureCoordinate).xy;\n" +
            "}";

    public static final String SHADER_NULL_FRAGMENT = "" +
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "varying vec2 textureCoordinate;\n" +
            "uniform samplerExternalOES sTexture;\n" +
            "void main() {\n" +
            "    vec4 tc = texture2D(sTexture, textureCoordinate);\n" +
            "    gl_FragColor = vec4(tc.r, tc.g, tc.b, 1.0);\n" +
            "}";

    public static final String LOOKUP_VERTEX_SHADER = "" +
            "attribute vec4 position;\n" +
            "attribute vec4 inputTextureCoordinate;\n" +
            "uniform   mat4 uPosMtx;\n" +
            "varying   vec2 textureCoordinate;\n" +
            "void main() {\n" +
            "  gl_Position = uPosMtx * position;\n" +
            "  textureCoordinate   = inputTextureCoordinate.xy;\n" +
            "}\n";

    public static final String LOOKUP_FRAGMENT_SHADER = ""+
            "precision mediump float;\n" +
            "varying highp vec2 textureCoordinate;\n" +
            " \n" +
            " uniform sampler2D inputImageTexture;\n" +
            " uniform sampler2D inputImageTexture2; // lookup texture\n" +
            " \n" +
            " uniform int lookupFlag;\n" +
            " void main()\n" +
            " {\n" +
            "     lowp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "     if(lookupFlag == 1) {\n" +
            "          \n" +
            "          mediump float blueColor = textureColor.b * 63.0;\n" +
            "          \n" +
            "          mediump vec2 quad1;\n" +
            "          quad1.y = floor(floor(blueColor) / 8.0);\n" +
            "          quad1.x = floor(blueColor) - (quad1.y * 8.0);\n" +
            "          \n" +
            "          mediump vec2 quad2;\n" +
            "          quad2.y = floor(ceil(blueColor) / 8.0);\n" +
            "          quad2.x = ceil(blueColor) - (quad2.y * 8.0);\n" +
            "          \n" +
            "          highp vec2 texPos1;\n" +
            "          texPos1.x = (quad1.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r);\n" +
            "          texPos1.y = (quad1.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g);\n" +
            "          \n" +
            "          highp vec2 texPos2;\n" +
            "          texPos2.x = (quad2.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r);\n" +
            "          texPos2.y = (quad2.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g);\n" +
            "          \n" +
            "          lowp vec4 newColor1 = texture2D(inputImageTexture2, texPos1);\n" +
            "          lowp vec4 newColor2 = texture2D(inputImageTexture2, texPos2);\n" +
            "          \n" +
            "          lowp vec4 newColor = mix(newColor1, newColor2, fract(blueColor));\n" +
            "          gl_FragColor = vec4(newColor.rgb, textureColor.w);\n" +
            "      } else {\n" +
            "          gl_FragColor = vec4(textureColor.rgb, 1.0);\n" +
            "      }\n" +
            " }";
}
