package codecoffe.restaurantes.xml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

public class LerStarterConfig {
	static final String IPSTART = "ip";

	public List<String> readConfig() throws FileNotFoundException, XMLStreamException {
		List<String> items = new ArrayList<String>();
		// First, create a new XMLInputFactory
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		// Setup a new eventReader
		InputStream in = new FileInputStream("starter.xml");
		XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
		// read the XML document
		while (eventReader.hasNext()) {
			XMLEvent event = eventReader.nextEvent();
			if (event.isStartElement()) 
			{
				if (event.asStartElement().getName().getLocalPart().equals(IPSTART)) {
					event = eventReader.nextEvent();
					items.add(event.toString());
					continue;
				}
			}
		}
		return items;
	}
}