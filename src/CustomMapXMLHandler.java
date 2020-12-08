import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.*;

/**
 * Handles XML events when parsing a custom map XML file
 *
 * @author Nicolas Tuttle
 */
public class CustomMapXMLHandler extends DefaultHandler {
    private final Stack<XMLTag> openTags;
    private final HashMap<XMLTag, String> continentAttributes;
    private final List<Territory> territoryList;
    private final HashMap<XMLTag, String> territoryAttributes;
    private final HashMap<String, Continent> customMap;

    /**
     * Constructor for class CustomMapXMLHandler. Initializes all helper states
     */
    public CustomMapXMLHandler() {
        openTags = new Stack<>();
        continentAttributes = new HashMap<>();
        territoryAttributes = new HashMap<>();
        customMap = new HashMap<>();
        territoryList = new ArrayList<>();
    }

    private boolean tagIsInvalid(String tag) {
        try {
            XMLTag.getTag(tag);
        } catch (IllegalArgumentException e) {
            return true;
        }

        return false;
    }

    /**
     * Returns the custom map generated from the XML file
     * @return The custom map described by the XML file
     * @throws SAXException if the map is invalid
     */
    public Map<String, Continent> getCustomMap() throws SAXException {
        assertValidMap();
        return customMap;
    }

    /**
     * Asserts whether the imported map is valid, throws a SAXException if there is an issue.
     * (all territories are reachable, all adjacency lists are valid, all territory IDs are unique)
     * @throws SAXException If the map is invalid
     */
    private void assertValidMap() throws SAXException {
        List<Territory> territories = new ArrayList<>();
        customMap.values().forEach(continent -> territories.addAll(continent.getTerritoryList()));

        Set<String> ids = new HashSet<>();
        for (Territory territory : territories) {
            ids.add(territory.getId());
            // Check that there are adjacent territories
            if (territory.getAdjacentList().isEmpty()) {
                throw new SAXException("Not all territories are reachable.");
            }
            // Check that all adjacency lists match up (Territory A is adjacent to Territory B and vice versa)
            // Territory also cannot be adjacent to itself
            for (String adjacent: territory.getAdjacentList()) {
                if (territories.stream().noneMatch(compare ->
                        compare != territory && compare.getId().equals(adjacent) && compare.getAdjacentList().contains(territory.getId()))
                ) {
                    throw new SAXException("Adjacency list invalid for " + territory.getId());
                }
            }
        }
        territories.forEach(territory -> ids.add(territory.getId()));
        // Check that all territory IDs are unique
        if (ids.size() != territories.size()) {
            throw new SAXException("Not all territory IDs are unique.");
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (tagIsInvalid(qName)) {
            throw new SAXException("Unknown tag in file");
        }

        XMLTag tag = XMLTag.getTag(qName);

        if ((openTags.size() == 0 && tag != XMLTag.CUSTOM_MAP) || (openTags.size() > 1 && !tag.hasParent(openTags.peek()))) {
            throw new SAXException("Invalid file structure");
        }

        openTags.push(tag);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (tagIsInvalid(qName) || openTags.peek() != XMLTag.getTag(qName)) {
            throw new SAXException("Unknown tag in file");
        }

        switch (openTags.pop()) {
            case CONTINENT:
                if (!(continentAttributes.containsKey(XMLTag.NAME)
                        && continentAttributes.containsKey(XMLTag.ID)
                        && continentAttributes.containsKey(XMLTag.BONUS_ARMIES)
                        && territoryList.size() > 0
                )) {
                    throw new SAXException("Missing sub-elements in Continent");
                }
                customMap.put(
                        continentAttributes.get(XMLTag.ID),
                        new Continent(
                                continentAttributes.get(XMLTag.NAME),
                                // Need to use copy of territory list as it is reused later on
                                List.copyOf(territoryList),
                                Integer.parseInt(continentAttributes.get(XMLTag.BONUS_ARMIES)))
                );

                continentAttributes.clear();
                territoryList.clear();
                break;
            case TERRITORY:
                if (!(territoryAttributes.containsKey(XMLTag.NAME)
                        && territoryAttributes.containsKey(XMLTag.ADJACENTS)
                )) {
                    throw new SAXException("Missing sub-elements in Territory");
                }

                String continentId = continentAttributes.get(XMLTag.ID);
                if (continentId == null) {
                    throw new SAXException("Could not find Continent ID");
                }

                territoryList.add(
                        new Territory(
                                territoryAttributes.get(XMLTag.NAME),
                                continentId + (territoryList.size() + 1),
                                Arrays.asList(territoryAttributes.get(XMLTag.ADJACENTS).split(",").clone())
                        )
                );

                territoryAttributes.clear();
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String chars = new String(ch, start, length);
        if (openTags.size() >= 2 && !chars.isBlank()) {
            switch (openTags.get(openTags.size() - 2)) {
                case TERRITORY:
                    territoryAttributes.put(openTags.peek(), chars);
                    break;
                case CONTINENT:
                    continentAttributes.put(openTags.peek(), chars);
                    break;
                default:
                    throw new SAXException("Unknown string attribute in tag " + openTags.peek() + ": " + chars);
            }
        }
    }
}
