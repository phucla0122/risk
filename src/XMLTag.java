import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An enum containing all XML tags that are parsed in a custom map.
 *
 * @author Nicolas Tuttle
 */
public enum XMLTag {
    CUSTOM_MAP("CustomMap", null),
    CONTINENT("Continent", Set.of(CUSTOM_MAP)),
    TERRITORIES("Territories", Set.of(CONTINENT)),
    TERRITORY("Territory", Set.of(TERRITORIES)),
    NAME("Name", Set.of(CONTINENT, TERRITORY)),
    ID("ID", Set.of(CONTINENT)),
    BONUS_ARMIES("BonusArmies", Set.of(CONTINENT)),
    ADJACENTS("Adjacents", Set.of(TERRITORY));

    String tag;
    Set<XMLTag> parent;

    /**
     * Constructor for XMLTag.
     * @param tag The tag text/name
     * @param parent A set containing possible parent tags
     */
    XMLTag(String tag, Set<XMLTag> parent) {
        this.tag = tag;
        this.parent = parent;
    }

    /**
     * Get the XMLTag instance for a given tag string
     * @param t The tag's string representation
     * @return The XMLTag instance corresponding to t
     */
    public static XMLTag getTag(String t) {
        List<XMLTag> tags = Arrays.stream(values()).filter(tag -> tag.tag.equals(t)).collect(Collectors.toList());
        if (tags.size() != 1) {
            throw new IllegalArgumentException("Tag not found with identifier " + t);
        }
        return tags.get(0);
    }

    @Override
    public String toString() {
        return tag;
    }

    /**
     * Checks whether tag t is a possible parent of this tag
     * @param t The parent candidate
     * @return True if t is a parent, false otherwise
     */
    public boolean hasParent(XMLTag t) {
        return this.parent.contains(t);
    }
}
