/**
 * @file:   com.nvisionary.gdx.ext.graphics.g3d.loaders.obj - FontObjLoader.java
 * @date:   May 23, 2012
 * @author: bweber
 */
package com.nvisionary.gdx.ext.graphics.g3d.loaders.obj;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.loaders.obj.ObjLoader;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

/**
 * 
 */
public class FontObjLoader {

    public static Map <Character, Mesh> loadObj (final InputStream in)
    {
        return loadObj (in, false);
    }
    
    public static Map <Character, Mesh> loadObj (final InputStream in, final boolean flipV)
    {
        return loadObj (in, flipV, false);
    }
    
    public static Map <Character, Mesh> loadObj (final InputStream in, final boolean flipV, final boolean useIndices)
    {
        String line = "";

        try {
            BufferedReader reader = new BufferedReader (new InputStreamReader (in));
            StringBuffer b = new StringBuffer ();
            String l = reader.readLine ();
            while (l != null) {
                b.append (l);
                b.append ("\n");
                l = reader.readLine ();
            }

            line = b.toString ();
            reader.close ();
        } catch (Exception ex) {
                return null;
        }
        return loadFontFromString (line, flipV, useIndices);
    }
    
    public static Map <Character, Mesh> loadFontFromString (final String obj, final boolean flipV, final boolean useIndices)
    {
        final String [] lines = obj.split ("\n");
        final List <String> vertices = new ArrayList <String> ();
        final List <String> normals = new ArrayList <String> ();
        final List <String[][]> faces = new ArrayList <String[][]> ();
        final Map <Character, Mesh> fontMap = new HashMap <Character, Mesh> ();
        
        vertices.add (null);
        normals.add (null);
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines [i];
            if (line.startsWith ("o ")) {
                String [] tokens = line.split ("\\.");
                if (tokens[1].isEmpty ()) {
                    tokens[1] = "._";
                }
                tokens = tokens[1].split ("_");
                if (tokens[0].isEmpty ()) {
                    tokens[0] = "_";
                }
                String tempObj = new String (lines [i++] + "\n");
                faces.clear ();
                do {
                    line = lines [i];
                    if (line.startsWith ("v ")) {
                        vertices.add (line);
                    } else if (line.startsWith ("vn ")) {
                        normals.add (line);
                    } else if (line.startsWith ("f ")) {
                        String[] fTokens = line.split("[ ]+");
                        String [][] face = new String [3][];
                        for (int j = 0; j < face.length; j++) {
                            String [] parts = fTokens[j+1].split("/");
                            face[j] = parts;
                        }
                        faces.add (face);
                    }
                } while ((lines.length > ++i) && !lines[i].startsWith ("o "));
                i--;
                
                List <String> usedVertices = new ArrayList <String> ();
                List <String> usedNormals = new ArrayList <String> ();
                Iterator<String[][]> faceIt = faces.iterator ();
                while (faceIt.hasNext ()) {
                    String [][] face = faceIt.next ();
                    String faceBack = "f";
                    for (int j = 0; j < face.length; j++) {
                        if (!usedVertices.contains (face[j][0])) {
                            usedVertices.add (face[j][0]);
                            tempObj += vertices.get (Integer.valueOf (face[j][0])) + "\n";
                            face [j][0] = String.valueOf (usedVertices.size ());
                        } else {
                            face [j][0] = String.valueOf (usedVertices.indexOf (face [j][0]) + 1);
                        }
                        if (!usedNormals.contains (face[j][2])) {
                            usedNormals.add (face[j][2]);
                            tempObj += normals.get (Integer.valueOf (face[j][2])) + "\n";
                            face [j][2] = String.valueOf (usedNormals.size ());
                        } else {
                            face [j][2] = String.valueOf (usedNormals.indexOf (face [j][2]) + 1);
                        }
                        faceBack += " " + face [j][0] + "/" + face [j][1] + "/" + face [j][2];
                    }
                    tempObj += faceBack + "\n";
                }
                Mesh tempMesh = ObjLoader.loadObjFromString (tempObj, flipV, useIndices);
                tempMesh = normalizeMeshPosition (tempMesh);
                fontMap.put (Character.valueOf (tokens[0].charAt (0)), tempMesh);
            }
        }
        
        return fontMap;
    }
    
    public static Mesh normalizeMeshPosition (Mesh font)
    {
        BoundingBox meshBB = font.calculateBoundingBox ();
        Vector3 centerVector = meshBB.getCenter ();
        centerVector.mul (-1.0f);
        
        /* seems to be the number of faces so times 6 (3 vertices, 3 normals) */
        int vertArraySize = font.getNumVertices () * 6;
        float[] tempVerts = new float [vertArraySize];
        font.getVertices (tempVerts);
        
        for (int i = 0; i < vertArraySize; i++) {
            tempVerts[i++] += centerVector.x;
            tempVerts[i++] += centerVector.y;
            tempVerts[i] += centerVector.z;
            /* skip the normals */
            i += 3;
        }
        
        font.setVertices (tempVerts);
        return font;
    }

}
