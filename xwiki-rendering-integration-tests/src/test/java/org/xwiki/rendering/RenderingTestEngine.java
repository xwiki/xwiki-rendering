package org.xwiki.rendering;

import org.apache.commons.io.FileUtils;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.engine.support.descriptor.FileSource;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

public class RenderingTestEngine
                implements TestEngine
{

    @Override
    public String getId()
    {
        return "xwiki-rendering-test-engine";
    }

    @Override
    public TestDescriptor discover( EngineDiscoveryRequest engineDiscoveryRequest, UniqueId uniqueId )
    {
        TestDescriptor engine = new EngineDescriptor( uniqueId, "XWiki Rendering TestEngine" );

        // TODO get "scope" from engineDiscoveryRequest.getConfigurationParameters()

        // TODO scan class-path/module-path for custom "@RenderingTestClass" annotated classes
        // TODO and get "scope" from custom annotation like @RenderingTestClass.Scope("simple")

        // TODO or find all "*.test" files via TCCL...

        findFile( "simple/escape/escape1.test" ).ifPresent( file -> add( engine, file ) );
        findFile( "simple/id/id.test" ).ifPresent( file -> add( engine, file ) );

        return engine;
    }

    private Optional<File> findFile( String name )
    {
        URL url = getClass().getClassLoader().getResource( name );
        if ( url == null )
        {
            return Optional.empty();
        }
        try
        {
            return Optional.of( new File( url.toURI() ) );
        }
        catch ( URISyntaxException e )
        {
            throw new RuntimeException( "URI ", e );
        }
    }

    private void add( TestDescriptor parent, File file )
    {
        parent.addChild( new Descriptor( parent.getUniqueId(), file ) );
    }

    @Override
    public void execute( ExecutionRequest request )
    {
        EngineExecutionListener listener = request.getEngineExecutionListener();
        TestDescriptor engine = request.getRootTestDescriptor();
        listener.executionFinished( engine, TestExecutionResult.successful() );
        listener.executionStarted( engine );
        for ( TestDescriptor child : engine.getChildren() )
        {
            listener.executionStarted( child );
            TestExecutionResult result = evaluate( (Descriptor) child );
            listener.executionFinished( child, result );
        }
    }

    private TestExecutionResult evaluate( Descriptor descriptor )
    {
        long size = FileUtils.sizeOf( descriptor.file );

        System.out.println( "TODO: evaluate ( " + size + " bytes of " + descriptor.file + " )" );

        if ( size % 5 == 0 )
        {
            return TestExecutionResult.aborted( null );
        }
        if ( size % 7 == 0 )
        {
            return TestExecutionResult.failed( null );
        }

        return TestExecutionResult.successful();
    }

    static class Descriptor
                    extends AbstractTestDescriptor
    {

        private final File file;

        Descriptor( UniqueId uniqueId, File file )
        {
            super( uniqueId.append( "descriptor", file.toString() ), "rendering: " + file, FileSource.from( file ) );
            this.file = file;
        }

        @Override
        public Type getType()
        {
            return Type.TEST;
        }
    }
}
