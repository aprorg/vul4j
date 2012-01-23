package org.codehaus.plexus.archiver.jar;

/*
 * Copyright  2001,2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import org.codehaus.plexus.PlexusTestCase;

/**
 * @author Emmanuel Venisse
 * @version $Id$
 */
public class ManifestTest
    extends PlexusTestCase
{
    public void testManifestReader1()
        throws Exception
    {
        Manifest manifest = getManifest( "src/test/resources/manifests/manifest1.mf" );
        String version = manifest.getManifestVersion();
        assertEquals( "Manifest was not created with correct version - ", "1.0", version );
    }

    public void testManifestReader2()
        throws Exception
    {
        try
        {
            getManifest( "src/test/resources/manifests/manifest2.mf" );
            fail( "Manifest isn't well formed. It must be generate an exception." );
        }
        catch ( ManifestException me )
        {
            if ( !me.getMessage().contains(
                "is not valid as it does not contain a name and a value separated by ': '" ) )
            {
                fail( "Manifest isn't well formed. It must generate an exception." );
            }
        }
    }

    public void testManifestReader3()
        throws Exception
    {
        try
        {
            getManifest( "src/test/resources/manifests/manifest3.mf" );
            fail( "Manifest isn't well formed. It must be generate an exception." );
        }
        catch ( ManifestException me )
        {
            if ( !me.getMessage().contains(
                "is not valid as it does not contain a name and a value separated by ': '" ) )
            {
                fail( "Manifest isn't well formed. It must generate an exception." );
            }
        }
    }

    public void testManifestReader4()
        throws Exception
    {
        Manifest manifest = getManifest( "src/test/resources/manifests/manifest4.mf" );
        Enumeration warnings = manifest.getWarnings();
        assertTrue( warnings.hasMoreElements() );
        String warn = (String) warnings.nextElement();
        assertFalse( warnings.hasMoreElements() );
        boolean hasWarning = warn.contains( "\"Name\" attributes should not occur in the main section" );
        assertEquals( "Expected warning about Name in main section", true, hasWarning );
    }

    public void testManifestReader5()
        throws Exception
    {
        try
        {
            getManifest( "src/test/resources/manifests/manifest5.mf" );
            fail();
        }
        catch ( ManifestException me )
        {
            boolean hasWarning = me.getMessage().contains( "Manifest sections should start with a \"Name\" attribute" );
            assertEquals( "Expected warning about section not starting with Name: attribute", true, hasWarning );
        }
    }

    public void testManifestReader6()
        throws Exception
    {
        Manifest manifest = getManifest( "src/test/resources/manifests/manifest6.mf" );
        Enumeration warnings = manifest.getWarnings();
        assertTrue( warnings.hasMoreElements() );
        String warn = (String) warnings.nextElement();
        assertFalse( warnings.hasMoreElements() );
        boolean hasWarning = warn.contains( "Manifest attributes should not start with \"From\"" );
        assertEquals( "Expected warning about From: attribute", true, hasWarning );
    }

    public void testGetDefaultManifest()
        throws Exception
    {
        Manifest mf = Manifest.getDefaultManifest();
        StringWriter writer = new StringWriter();
        mf.write( new PrintWriter( writer ) );
        String s = writer.toString();
        assertTrue( s.contains( "Manifest-Version" )  );
        assertTrue( s.contains( "Created-By" )  );
        assertTrue( s.contains( "Archiver-Version" )  );
    }

    public void testAttributeLongLineWrite()
        throws Exception
    {
        StringWriter writer = new StringWriter();
        Manifest.Attribute attr = new Manifest.Attribute();
        String longLineOfChars = "123456789 123456789 123456789 123456789 123456789 123456789 123456789 " +
                                 "123456789 123456789 123456789 ";
        attr.setName( "test" );
        attr.setValue( longLineOfChars );
        attr.write( new PrintWriter( writer ) );
        writer.flush();
        assertEquals( "should be multiline",
                      "test: 123456789 123456789 123456789 123456789 123456789 123456789 1234"
                          + Manifest.EOL +
                          " 56789 123456789 123456789 123456789 " + Manifest.EOL,
                      writer.toString() );
    }


    public void testDualClassPath()
        throws ManifestException, IOException
    {
        Manifest manifest = getManifest( "src/test/resources/manifests/manifestWithDualClassPath.mf" );
        final Manifest.Attribute attribute = manifest.getMainSection().getAttribute( "Class-Path" );
        assertEquals( "../config/ classes12.jar baz", attribute.getValue() );
    }

    public void testAttributeMultiLineValue()
        throws Exception
    {
        checkMultiLineAttribute( "123456789" + Manifest.EOL + "123456789",
                                 "123456789" + Manifest.EOL + " 123456789" + Manifest.EOL );
    }

    public void testAttributeDifferentLineEndings()
        throws Exception
    {
        checkMultiLineAttribute(
            "\tA\rB\n\t C\r\n \tD\n\r",
            "\tA" + Manifest.EOL +
                " B" + Manifest.EOL +
                " \t C" + Manifest.EOL +
                "  \tD" + Manifest.EOL );
    }
    
    public void testIterators()
        throws ManifestException, IOException
    {
        Manifest manifest = getManifest( "src/test/resources/manifests/manifestMerge1.mf" );
        String key = manifest.getMainSection().getAttributeKeys().nextElement();
        // Unsure if we can assert on the value here. Maps and ordering and all that
        assertEquals( "bar", key );
    }

    public void testDefaultBehaviour()
    {
        Manifest manifest = new Manifest(  );
        Manifest.Section mainSection = manifest.getMainSection();
        assertNotNull( mainSection );
        Manifest.Attribute bar = mainSection.getAttribute( "Bar" );
        assertNull( bar);
        assertNull( manifest.getSection( "Fud" ) );
    }

    public void checkMultiLineAttribute( String in, String expected )
        throws Exception
    {
        StringWriter writer = new StringWriter();
        Manifest.Attribute attr = new Manifest.Attribute();
        attr.setName( "test" );
        attr.setValue( in );
        attr.write( new PrintWriter( writer ) );
        writer.flush();

        // Print the string with whitespace replaced with special codes
        // so in case of failure you can see what went wrong.
        System.err.println( "String: " + dumpString( writer.toString() ) );

        assertEquals( "should be indented multiline",
                      "test: " + expected, writer.toString() );
    }

    private static String dumpString( String in )
    {
        String out = "";

        char [] chars = in.toCharArray();

        for ( char aChar : chars )
        {
            switch ( aChar )
            {
                case '\t':
                    out += "\\t";
                    break;
                case '\r':
                    out += "\\r";
                    break;
                case '\n':
                    out += "\\n";
                    break;
                case ' ':
                    out += "\\s";
                    break;
                default:
                    out += aChar;
                    break;
            }
        }

        return out;
    }

    public void testAddAttributesPlexusManifest()
        throws ManifestException, IOException
    {
        Manifest manifest = getManifest( "src/test/resources/manifests/manifestMerge1.mf" );
        Manifest.Section fudz = manifest.getSection( "Fudz" );
        fudz.addConfiguredAttribute( new Manifest.Attribute( "boz", "bzz" ) );
        assertEquals( "bzz", manifest.getSection( "Fudz" ).getAttributeValue( "boz" ) );
    }

    public void testRemoveAttributesPlexusManifest()
        throws ManifestException, IOException
    {
        Manifest manifest = getManifest( "src/test/resources/manifests/manifestMerge1.mf" );
        Manifest.Section fudz = manifest.getSection( "Fudz" );
        fudz.addConfiguredAttribute( new Manifest.Attribute( "boz", "bzz" ) );
        assertEquals( "bzz", fudz.getAttributeValue( "boz" ) );
        fudz.removeAttribute( "boz" );
        assertNull( fudz.getAttributeValue( "boz" ) );
    }

    public void testAttributeSerializationPlexusManifest()
        throws IOException, ManifestException
    {
        Manifest manifest = new Manifest(  );
        manifest.getMainSection().addConfiguredAttribute( new Manifest.Attribute( "mfa1", "fud1" ) );
        manifest.getMainSection().addConfiguredAttribute( new Manifest.Attribute( "mfa2", "fud2" ) );
        Manifest.Section attributes = new Manifest.Section(  );
        attributes.setName( "TestSection" );
        attributes.addConfiguredAttribute( new Manifest.Attribute( "attA", "baz" ) );
        attributes.addConfiguredAttribute( new Manifest.Attribute( "attB", "caB" ) );
        manifest.addConfiguredSection(  attributes );
        StringWriter writer = new StringWriter();
        manifest.write(  new PrintWriter( writer )  );
        String s = writer.toString();
        assertTrue( s.contains( "mfa1: fud1" ) );
        assertTrue( s.contains( "mfa2: fud2" ) );
        assertTrue( s.contains( "attA: baz" ) );
        assertTrue( s.contains( "attB: caB" ) );
    }

    public void testClassPathPlexusManifest()
        throws ManifestException
    {
        Manifest manifest = new Manifest();
        manifest.addConfiguredAttribute(
            new Manifest.Attribute( ManifestConstants.ATTRIBUTE_CLASSPATH, "fud" ) );
        manifest.addConfiguredAttribute(
            new Manifest.Attribute( ManifestConstants.ATTRIBUTE_CLASSPATH, "duf" ) );
        assertEquals( "fud duf", manifest.getMainSection().getAttributeValue(
            ManifestConstants.ATTRIBUTE_CLASSPATH ) );
    }

    public void testAddConfiguredSectionPlexusManifest()
        throws ManifestException
    {
        Manifest manifest = new Manifest();
        Manifest.Section section = new Manifest.Section();
        section.setName( "fud" );
        section.addConfiguredAttribute( new Manifest.Attribute( "bar", "baz" ) );
        manifest.addConfiguredSection( section );
        assertEquals( "baz", manifest.getSection( "fud" ).getAttribute( "bar" ).getValue() );
    }

    /**
     * Reads a Manifest file.
     * @param filename the file
     * @return a manifest
     * @throws java.io.IOException .
     * @throws ManifestException .
     */
    private Manifest getManifest( String filename )
        throws IOException, ManifestException
    {
        FileReader r = new FileReader( getTestFile( filename ) );

        try
        {
            return new Manifest( r );
        }
        finally
        {
            r.close();
        }
    }
}
