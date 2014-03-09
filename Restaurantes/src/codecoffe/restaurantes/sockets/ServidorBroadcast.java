package codecoffe.restaurantes.sockets;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

import codecoffe.restaurantes.graficos.PainelErro;

public class ServidorBroadcast extends Thread
{
	private int porta;
	private boolean serverON;
	//private ArrayList<String> hostsPossiveis;
	private DatagramSocket socket;
	
	public ServidorBroadcast(int porta)
	{
		this.porta = porta;
		//this.hostsPossiveis = new ArrayList<>();
		this.serverON = true;
	}
	
	public boolean isServerON() {
		return serverON;
	}

	public void setServerON(boolean serverON) {
		this.serverON = serverON;
		if(!serverON)
			this.socket.close();
	}

	public void run()
	{
		try
		{
			/*Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			
			while(interfaces.hasMoreElements()) 
			{
				NetworkInterface networkInterface = interfaces.nextElement();
				if(networkInterface.isLoopback())
					continue;
				 
				for(InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses())
				{
					InetAddress broadcast = interfaceAddress.getBroadcast();
					if (broadcast == null)
						continue;
					
					hostsPossiveis.add(broadcast.getHostAddress());
				}
			}
			
			hostsPossiveis.add("192.168.1.255");*/
			
			ArrayList<String> listaBroadcasts = pegaBroadcasts();
			
			this.socket = new DatagramSocket(this.porta);
			System.out.println("Server UDP iniciado na porta: " + this.porta);
			
			while(this.serverON)
			{
				/*for(int i = 0; i < hostsPossiveis.size(); i++)
				{
					DatagramPacket packet = new DatagramPacket(new byte[] {(byte) 0xF0}, 1, InetAddress.getByName(hostsPossiveis.get(i)), this.porta);
					this.socket.send(packet);
				}*/
				
				for(int i = 0; i < listaBroadcasts.size(); i++)
				{
					DatagramPacket packet = new DatagramPacket(new byte[] {(byte) 0xF0}, 1, InetAddress.getByName(listaBroadcasts.get(i)), this.porta);
					this.socket.send(packet);
				}				
				
				sleep((long)(Math.random() * 2000));
			}
			
			this.socket.close();
		}
		catch(IOException | InterruptedException e)
		{
			e.printStackTrace();
			//new PainelErro(e);
		}
	}
	
	public ArrayList<String> pegaBroadcasts()
	{
        ArrayList<String> listOfBroadcasts = new ArrayList<>();
        Enumeration<?> list;
        try {
            list = NetworkInterface.getNetworkInterfaces();

            while(list.hasMoreElements()) {
                NetworkInterface iface = (NetworkInterface) list.nextElement();
                if(iface == null) continue;

                if(!iface.isLoopback() && iface.isUp()) {
                    Iterator<InterfaceAddress> it = iface.getInterfaceAddresses().iterator();
                    while (it.hasNext()) {
                        InterfaceAddress address = (InterfaceAddress) it.next();
                        if(address == null) continue;
                        InetAddress broadcast = address.getBroadcast();
                        if(broadcast != null) 
                        {
                            //System.out.println("Broadcast encontrado: " + broadcast.getHostAddress());
                            listOfBroadcasts.add(broadcast.getHostAddress());
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
            new PainelErro(ex);
            System.exit(0);
        }
        return listOfBroadcasts;
	}
}