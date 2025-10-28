package net.ltxprogrammer.changed.init;

import net.ltxprogrammer.changed.Changed;
import net.ltxprogrammer.changed.world.features.structures.facility.Zone;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Function;

/**
 * Define identifiable areas in facility generation that group pieces together.
 * It is planned to allow latexes to spawn naturally in their respective zones.
 */
public class ChangedFacilityZones {
    public static final DeferredRegister<Zone> REGISTRY = ChangedRegistry.FACILITY_ZONES.createDeferred(Changed.MODID);

    private static RegistryObject<Zone> registerZone(String name, Function<ResourceLocation, Zone> ctor) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Changed.MODID, name);
        return REGISTRY.register(name, () -> ctor.apply(id));
    }

    /**
     * First area seen in Changed. A transitory area that is usually seen in between other zones.
     * <ul>
     *     <li>Blue tiled flooring</li>
     *     <li>Gray striped walls</li>
     *     <li>Rooms for experimentation, research, and stasis</li>
     *     <li>Home to a large variety of white latex based organisms</li>
     * </ul>
     */
    public static final RegistryObject<Zone> GENERAL_ZONE = registerZone("general", Zone.withParam(-9999, 9999, 1f, 1));
    /**
     * A server farm, filled with servers, boxes, and fans.
     * <ul>
     *     <li>White tiled flooring</li>
     *     <li>Dark green walls with white squiggles</li>
     *     <li>Rooms for experimentation, research, and stasis</li>
     *     <li>Contested area between white and dark latex pups</li>
     *     <li>Secured with restricting lasers</li>
     *     <li>Servers, fans and boxes everywhere</li>
     * </ul>
     */
    public static final RegistryObject<Zone> SERVER_ZONE = registerZone("server", Zone.withParam(28, 32, 2f, 3));
    /**
     * A more residential area fit for typical office life with computers.
     * <ul>
     *     <li>White tiled flooring</li>
     *     <li>Red striped walls</li>
     *     <li>Black tables</li>
     *     <li>Home to dark latexes</li>
     * </ul>
     */
    public static final RegistryObject<Zone> OFFICE_ZONE = registerZone("office", Zone.withParam(24, 30, 2f, 4));
    /**
     * Portion of the office zone that has been completely overtaken by dark latexes.
     * <ul>
     *     <li>Dark latex on all surfaces, covering office decor</li>
     *     <li>Gray and green crystals</li>
     *     <li>Slippery ice and springy "orbs"</li>
     *     <li>Home to dark latexes</li>
     * </ul>
     */
    public static final RegistryObject<Zone> DARK_LATEX_ZONE = registerZone("dark_latex", Zone.withParam(20, 26, 2f, 2));
    /**
     * Books.
     * <ul>
     *     <li>Brown and tan floor tiling, with orange carpeting</li>
     *     <li>Brown and tan striped walls</li>
     *     <li>Bookshelves and computers</li>
     *     <li>Home to ceiling dwelling kade, and circle</li>
     * </ul>
     */
    public static final RegistryObject<Zone> LIBRARY_ZONE = registerZone("library", Zone.withParam(18, 22, 2f, 3));
    /**
     * Backend area that is generally not meant for foot traffic. Connects all places of the facility together.
     * <ul>
     *     <li>Metal flooring and walls</li>
     *     <li>Some boxes and water puddles</li>
     *     <li>An actual home to one particular dark latex</li>
     *     <li>Home to spiders, snakes, moths, etc.</li>
     * </ul>
     */
    public static final RegistryObject<Zone> VENT_ZONE = registerZone("vent", Zone.withParam(8, 20, 1f, 1));
    /**
     * Botanical area filled with grass, plants, and shrubs.
     * <ul>
     *     <li>Wooden flooring and walls</li>
     *     <li>Large orange shrubs, planters, acidic electric generator</li>
     *     <li>Home to latex leaf, mimic plant, squirrel</li>
     * </ul>
     */
    public static final RegistryObject<Zone> GREENHOUSE_ZONE = registerZone("greenhouse", Zone.withParam(14, 16, 2f, 3));
    /**
     * Electrical area flooded by water.
     * <ul>
     *     <li>Blue tiled flooring</li>
     *     <li>Gray, white, and hazard striped walls</li>
     *     <li>Generators</li>
     *     <li>Exosuits, shipping containers, pipes</li>
     *     <li>Home to sharks and squid dogs</li>
     * </ul>
     */
    public static final RegistryObject<Zone> MAINTENANCE_ZONE = registerZone("maintenance", Zone.withParam(10, 14, 2f, 4));
    /**
     * Portion of the general zone that has been completely overtaken by white latexes. "White latex jungle"
     * <ul>
     *     <li>White latex on all surfaces</li>
     *     <li>Moving white latex walls, pillars, "bushes"</li>
     *     <li>Home to pure white latexes, behemoths</li>
     * </ul>
     */
    public static final RegistryObject<Zone> WHITE_LATEX_ZONE = registerZone("white_latex", Zone.withParam(6, 8, 2f, 2));
    /**
     * First zone seen when entering the facility. Still being developed in Changed: SE
     * <ul>
     *     <li>Blue diagonal tiles</li>
     *     <li>Light green diagonal walls</li>
     * </ul>
     */
    public static final RegistryObject<Zone> ENTRANCE_ZONE = registerZone("entrance", Zone.withParam(0, 4, 2f, 4));
}
