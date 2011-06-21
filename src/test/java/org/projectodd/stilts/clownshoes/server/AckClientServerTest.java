/*
 * Copyright 2008-2011 Red Hat, Inc, and individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.projectodd.stilts.clownshoes.server;

import static org.junit.Assert.*;

import org.junit.Test;
import org.projectodd.stilts.StompMessages;
import org.projectodd.stilts.logging.SimpleLoggerManager.Level;
import org.projectodd.stilts.spi.Subscription.AckMode;
import org.projectodd.stilts.stomp.client.ClientSubscription;
import org.projectodd.stilts.stomp.client.ClientTransaction;

public class AckClientServerTest extends AbstractStompletClientServerTest {

    static {
        SERVER_ROOT_LEVEL = Level.TRACE;
        CLIENT_ROOT_LEVEL = Level.NONE;
    }

    @Test
    public void testClientTransaction() throws Exception {
        client.connect();

        ClientSubscription subscription1 = client.subscribe( "/queues/foo" ).withMessageHandler( accumulator( "one", false, true ) ).withAckMode( AckMode.CLIENT_INDIVIDUAL ).start();
        ClientSubscription subscription2 = client.subscribe( "/queues/foo" ).withMessageHandler( accumulator( "two" ) ).withAckMode( AckMode.AUTO ).start();
        
        ClientTransaction tx = client.begin();

        for (int i = 0; i < 10; ++i) {
            tx.send( StompMessages.createStompMessage( "/queues/foo", "What? " + i ) );
        }

        Thread.sleep( 1000 );
        assertTrue( accumulator( "one" ).isEmpty() );
        assertTrue( accumulator( "two" ).isEmpty() );
        tx.commit();
        Thread.sleep( 1000 );
        subscription1.unsubscribe();
        Thread.sleep( 1000 );
        subscription2.unsubscribe();
        client.disconnect();
        
        System.err.println( "===========================================" );
        System.err.println( "===========================================" );
        System.err.println( accumulator("one").messageIds() );
        System.err.println( accumulator("two").messageIds() );
        System.err.println( "===========================================" );
        System.err.println( "===========================================" );
        assertEquals( 10, accumulator( "two" ).size() );
        
        
    }
}