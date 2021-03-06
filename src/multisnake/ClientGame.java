/*
 *  Copyright (C) 2010 Patrick Hulin
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package multisnake;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.List;
import java.net.Socket;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import java.awt.event.KeyListener;
import java.util.Arrays;

/**
 *
 * @author Patrick Hulin
 */
public class ClientGame implements Runnable, KeyListener {
    private BoardCanvas bc;
    private JTable scoreBoard;

    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private Thread waitingThread;

    public ClientGame(BoardCanvas bc,
                      JTable scoreBoard) {

        this.bc = bc;
        this.scoreBoard = scoreBoard;
    }

    public void runGame(String host, int port) {
        try {
            socket = new Socket(host, port);

            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
        } catch(IOException ex) {
            ex.printStackTrace();
            System.exit(1);
        }

        waitingThread = new Thread(this);
        waitingThread.start();

        bc.addKeyListener(this);
    }

    public void run() {
        while(!socket.isClosed()) {
            TickPacket tp = null;

            try {
                tp = (TickPacket)(inputStream.readObject());

                List<Player> players = Arrays.asList(tp.getPlayers());
                List<Pickup> pickups = Arrays.asList(tp.getPickups());

                bc.initForGame(players, pickups);
                bc.repaint();

                ScoreBoardModel nsbm = new ScoreBoardModel(players);
                ScoreBoardUpdate sbu = new ScoreBoardUpdate(scoreBoard, nsbm);
                SwingUtilities.invokeLater(sbu);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        if(key == KeyEvent.VK_UP)
            setDirection(Direction.NORTH);
        else if(key == KeyEvent.VK_RIGHT)
            setDirection(Direction.EAST);
        else if(key == KeyEvent.VK_DOWN)
            setDirection(Direction.SOUTH);
        else if(key == KeyEvent.VK_LEFT)
            setDirection(Direction.WEST);
    }

    public void setDirection(Direction dir) {
        try {
            outputStream.writeObject(dir);
            outputStream.flush();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    // unused methods
    public void keyTyped(KeyEvent e) { }
    public void keyPressed(KeyEvent e) { }
}
