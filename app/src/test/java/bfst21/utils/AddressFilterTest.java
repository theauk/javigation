package bfst21.utils;

import bfst21.Osm_Elements.Node;
import bfst21.data_structures.AddressTriesTree;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AddressFilterTest {

    private static AddressFilter addressFilter;
    private static AddressTriesTree addressTriesTree;

    @BeforeAll
    public static void setup() {
        addressTriesTree = new AddressTriesTree();
        addressFilter = new AddressFilter();
        addressFilter.setAddressTree(addressTriesTree);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(AddressFilterTest.class.getResourceAsStream("/tests/addresses.txt"), StandardCharsets.UTF_8))) {
            String line;
            int id = 0;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");

                String street = data[0].strip().toLowerCase();
                String number = data[1].strip().toLowerCase();
                int postCode = Integer.parseInt(data[2].strip());
                String city = data[3].strip().toLowerCase();
                Node node = new Node(id, id, id);

                addressTriesTree.put(node, street, number, postCode, city);

                id++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSuggestionMatchOnStreetStaringWithDanishCharacters() {
        search("Æ");
        List<String> suggestions = getSuggestions();
        assertEquals(3, getSuggestionsSize());
        assertEquals("æ gammel havn, 8752 østbirk", suggestions.get(0));
        assertEquals("æ-drøwt, 8752 østbirk", suggestions.get(1));
        assertEquals("ærøvej, 3720 aakirkeby", suggestions.get(2));
    }

    //NO SUGGESTIONS
    @Test
    public void testForNoSuggestionsForEmptyOrBlankStrings() {
        String prefix = "";
        for(int i = 0; i < 100; i++) {
            search(prefix);
            assertEquals(0, getSuggestionsSize());

            prefix += " ";
        }
    }

    @Test
    public void testForNoSuggestionsForWrongHouseNumberAndPostCodeWithFullAddress() {
        String prefix = "Banevej 79, 2400 København NV";
        search(prefix);
        assertEquals(0, getSuggestionsSize());
    }

    @Test
    public void testForNoSuggestionsForWrongHouseNumberWithStreetOnly() {
        String prefix = "Cirose Allé 4";
        search(prefix);
        assertEquals(0, getSuggestionsSize());
    }

    @Test
    public void testSuggestionsSearchOnSpecificAddressForStreet() {
        String prefix = "Østbanevej";
        String search = "";

        search("");
        assertEquals(0, getSuggestionsSize());

        for(int i = 0; i < prefix.length(); i++) {
            search += prefix.charAt(i);
            search(search);

            assertEquals(1, getSuggestionsSize());
            assertEquals("østbanevej, 5600 faaborg", getSuggestions().get(0));
        }
    }

    //TYPING
    @Test
    public void testSuggestionsSearchOnSpecificAddressForStreetAndNumber() {
        String prefix = "Østbanevej 8";
        String search = "";

        search("");
        assertEquals(0, getSuggestionsSize());

        for(int i = 0; i < prefix.length(); i++) {
            search += prefix.charAt(i);
            search(search);

            assertEquals(1, getSuggestionsSize());

            if(i < 11) assertEquals("østbanevej, 5600 faaborg", getSuggestions().get(0));
            else assertEquals("østbanevej 8, 5600 faaborg", getSuggestions().get(0));
        }
    }

    @Test
    public void testSuggestionsSearchForSpecificStreetWithMoreMatches() {
        String prefix = "Studiestræde";
        StringBuilder search = new StringBuilder();

        search("");
        assertEquals(0, getSuggestionsSize());

        for(int i = 0; i < prefix.length(); i++) {
            search.append(prefix.charAt(i));
            search(search.toString());

            if(i == 0) {
                assertEquals(4, getSuggestionsSize());
                assertEquals("slarranfen, 9230 svenstrup j", getSuggestions().get(0));
                assertEquals("studiestræde, 1455 københavn k", getSuggestions().get(1));
                assertEquals("studiestræde, 4000 roskilde", getSuggestions().get(2));
                assertEquals("studievej, 1455 københavn k", getSuggestions().get(3));
            }
            else if(i <= 5) {
                assertEquals(3, getSuggestionsSize());
                assertEquals("studiestræde, 1455 københavn k", getSuggestions().get(0));
                assertEquals("studiestræde, 4000 roskilde", getSuggestions().get(1));
                assertEquals("studievej, 1455 københavn k", getSuggestions().get(2));
            } else {
                assertEquals(2, getSuggestionsSize());
                assertEquals("studiestræde, 1455 københavn k", getSuggestions().get(0));
                assertEquals("studiestræde, 4000 roskilde", getSuggestions().get(1));
            }
        }
    }

    @Test
    public void testSuggestionsByTypingSpecificAddress() {
        String prefix = "Studiestræde 1, 1455 København K";
        StringBuilder typed = new StringBuilder();

        //Check for empty string
        search("");
        assertEquals(0, getSuggestionsSize());

        //Simulate typing of prefix
        for(int i = 0; i < prefix.length(); i++) {
            typed.append(prefix.charAt(i));
            search(typed.toString());

            if(i == 0) { //"S"
                assertEquals(4, getSuggestionsSize());
                assertEquals("slarranfen, 9230 svenstrup j", getSuggestions().get(0));
                assertEquals("studiestræde, 1455 københavn k", getSuggestions().get(1));
                assertEquals("studiestræde, 4000 roskilde", getSuggestions().get(2));
                assertEquals("studievej, 1455 københavn k", getSuggestions().get(3));
            }
            else if(i <= 5) {   //"Studie"
                assertEquals(3, getSuggestionsSize());
                assertEquals("studiestræde, 1455 københavn k", getSuggestions().get(0));
                assertEquals("studiestræde, 4000 roskilde", getSuggestions().get(1));
                assertEquals("studievej, 1455 københavn k", getSuggestions().get(2));
            }
            else if(i <= 12) {  //"Studiestræde "
                assertEquals(2, getSuggestionsSize());
                assertEquals("studiestræde, 1455 københavn k", getSuggestions().get(0));
                assertEquals("studiestræde, 4000 roskilde", getSuggestions().get(1));
            }
            else if(i <= 15) {  //"Studestræde 1, "
                assertEquals(4, getSuggestionsSize());
                assertEquals("studiestræde 18, 1455 københavn k", getSuggestions().get(0));
                assertEquals("studiestræde 18, 4000 roskilde", getSuggestions().get(1));
                assertEquals("studiestræde 18a, 1455 københavn k", getSuggestions().get(2));
                assertEquals("studiestræde 18d, 1455 københavn k", getSuggestions().get(3));
            }
            else if(i <= 20) {  //"Studiestræde 1, 1455 "
                assertEquals(3, getSuggestionsSize());
                assertEquals("studiestræde 18, 1455 københavn k", getSuggestions().get(0));
                assertEquals("studiestræde 18a, 1455 københavn k", getSuggestions().get(1));
                assertEquals("studiestræde 18d, 1455 københavn k", getSuggestions().get(2));
            }
            else {  //"Studiestræde 1, 1455 København K"
                assertEquals(3, getSuggestionsSize());
                assertEquals("studiestræde 18, 1455 københavn k", getSuggestions().get(0));
                assertEquals("studiestræde 18a, 1455 københavn k", getSuggestions().get(1));
                assertEquals("studiestræde 18d, 1455 københavn k", getSuggestions().get(2));
            }
        }
    }

    //SPECIAL CHARACTERS AND WHITESPACE
    @Test
    public void testWhiteSpaceIsIgnored() {
        String prefix = "   Østbanevej    8    ,    5600     Faaborg   ";
        search(prefix);
        assertNotNull(addressFilter.getMatchedAddress());
        assertEquals(1, getSuggestionsSize());
        assertEquals("østbanevej", addressFilter.getMatchedAddress().getStreet());
        assertEquals("8", addressFilter.getMatchedAddress().getHouseNumber());
        assertEquals(5600, addressFilter.getMatchedAddress().getPostCode());
        assertEquals("faaborg", addressFilter.getMatchedAddress().getCity());
        assertEquals("østbanevej 8, 5600 faaborg", addressFilter.getMatchedAddress().toString());
    }

    @Test
    public void testStreetsWithWhiteSpaceMatches() {
        String prefix = "Gl. Kongevej 123, 1610 København V";
        search(prefix);
        assertNotNull(addressFilter.getMatchedAddress());
        assertEquals(1, getSuggestionsSize());
        assertEquals("gl. kongevej", addressFilter.getMatchedAddress().getStreet());
        assertEquals("123", addressFilter.getMatchedAddress().getHouseNumber());
        assertEquals(1610, addressFilter.getMatchedAddress().getPostCode());
        assertEquals("københavn v", addressFilter.getMatchedAddress().getCity());
        assertEquals("gl. kongevej 123, 1610 københavn v", addressFilter.getMatchedAddress().toString());

        prefix = "Æ Gammel Havn 1, 8752 Østbirk";
        search(prefix);
        assertNotNull(addressFilter.getMatchedAddress());
        assertEquals(1, getSuggestionsSize());
        assertEquals("æ gammel havn", addressFilter.getMatchedAddress().getStreet());
        assertEquals("1", addressFilter.getMatchedAddress().getHouseNumber());
        assertEquals(8752, addressFilter.getMatchedAddress().getPostCode());
        assertEquals("østbirk", addressFilter.getMatchedAddress().getCity());
        assertEquals("æ gammel havn 1, 8752 østbirk", addressFilter.getMatchedAddress().toString());
    }

    @Test
    public void testStreetsWithApostropheMatches() {
        String prefix = "Heinos' gade 233, 7130 Juelsminde";
        search(prefix);
        assertNotNull(addressFilter.getMatchedAddress());
        assertEquals(1, getSuggestionsSize());
        assertEquals("heinos' gade", addressFilter.getMatchedAddress().getStreet());
        assertEquals("233", addressFilter.getMatchedAddress().getHouseNumber());
        assertEquals(7130, addressFilter.getMatchedAddress().getPostCode());
        assertEquals("juelsminde", addressFilter.getMatchedAddress().getCity());
    }

    @Test
    public void testStreetsWithAcuteAccentMatches() {
        String prefix = "Møns Allé 135, 9990 Skagen";
        search(prefix);
        assertNotNull(addressFilter.getMatchedAddress());
        assertEquals(1, getSuggestionsSize());
        assertEquals("møns allé", addressFilter.getMatchedAddress().getStreet());
        assertEquals("135", addressFilter.getMatchedAddress().getHouseNumber());
        assertEquals(9990, addressFilter.getMatchedAddress().getPostCode());
        assertEquals("skagen", addressFilter.getMatchedAddress().getCity());
    }

    @Test
    public void testStreetsWithHyphenMatches() {
        String prefix = "Æ-Drøwt 777, 8752 Østbirk";
        search(prefix);
        assertNotNull(addressFilter.getMatchedAddress());
        assertEquals(1, getSuggestionsSize());
        assertEquals("æ-drøwt", addressFilter.getMatchedAddress().getStreet());
        assertEquals("777", addressFilter.getMatchedAddress().getHouseNumber());
        assertEquals(8752, addressFilter.getMatchedAddress().getPostCode());
        assertEquals("østbirk", addressFilter.getMatchedAddress().getCity());
    }

    @Test
    public void testStreetWithNumberMatches() {
        String prefix1 = "10. Februarvej 999, 6070 Christiansfeld";
        String prefix2 = "Februar 7 Vej 999, 6070 Christiansfeld";
        String prefix3 = "Februarvej 6, 999, 6070 Christiansfeld";

        //Starting with a number
        search(prefix1);
        assertNotNull(addressFilter.getMatchedAddress());
        assertEquals(1, getSuggestionsSize());
        assertEquals("10. februarvej", addressFilter.getMatchedAddress().getStreet());
        assertEquals("999", addressFilter.getMatchedAddress().getHouseNumber());
        assertEquals(6070, addressFilter.getMatchedAddress().getPostCode());
        assertEquals("christiansfeld", addressFilter.getMatchedAddress().getCity());

        //Mid number
        search(prefix2);
        assertNotNull(addressFilter.getMatchedAddress());
        assertEquals(1, getSuggestionsSize());
        assertEquals("februar 7 vej", addressFilter.getMatchedAddress().getStreet());
        assertEquals("999", addressFilter.getMatchedAddress().getHouseNumber());
        assertEquals(6070, addressFilter.getMatchedAddress().getPostCode());
        assertEquals("christiansfeld", addressFilter.getMatchedAddress().getCity());

        //End number
        search(prefix3);
        assertNotNull(addressFilter.getMatchedAddress());
        assertEquals(1, getSuggestionsSize());
        assertEquals("februarvej 6", addressFilter.getMatchedAddress().getStreet());
        assertEquals("999", addressFilter.getMatchedAddress().getHouseNumber());
        assertEquals(6070, addressFilter.getMatchedAddress().getPostCode());
        assertEquals("christiansfeld", addressFilter.getMatchedAddress().getCity());
    }

    @Test
    public void testStreetWithUmlautMatches() {
        String prefix1 = "Härnösandvej 66,  7100 Vejle";
        String prefix2 = "Klüwersgade 7, 8370 Hadsten";

        //Prefix 1: Ä/Ö
        search(prefix1);
        assertNotNull(addressFilter.getMatchedAddress());
        assertEquals("härnösandvej", addressFilter.getMatchedAddress().getStreet());
        assertEquals("66", addressFilter.getMatchedAddress().getHouseNumber());
        assertEquals(7100, addressFilter.getMatchedAddress().getPostCode());
        assertEquals("vejle", addressFilter.getMatchedAddress().getCity());

        //Prefix 2: Ü
        search(prefix2);
        assertNotNull(addressFilter.getMatchedAddress());
        assertEquals("klüwersgade", addressFilter.getMatchedAddress().getStreet());
        assertEquals("7", addressFilter.getMatchedAddress().getHouseNumber());
        assertEquals(8370, addressFilter.getMatchedAddress().getPostCode());
        assertEquals("hadsten", addressFilter.getMatchedAddress().getCity());
    }

    @Test
    public void testStreetWithDanishCharactersMatches() {
        String prefix1 = "Kælkestræde 2 8305 Samsø";
        String prefix2 = "Åboulevarden 875, 4296 Nyrup";
        String prefix3 = "Østbanevej 8, 5600 Faaborg";

        //Prefix 1: Æ/Ø
        search(prefix1);
        assertNotNull(addressFilter.getMatchedAddress());
        assertEquals(1, getSuggestionsSize());
        assertEquals("kælkestræde", addressFilter.getMatchedAddress().getStreet());
        assertEquals("2", addressFilter.getMatchedAddress().getHouseNumber());
        assertEquals(8305, addressFilter.getMatchedAddress().getPostCode());
        assertEquals("samsø", addressFilter.getMatchedAddress().getCity());

        //Prefix 2: Å
        search(prefix2);
        assertNotNull(addressFilter.getMatchedAddress());
        assertEquals(1, getSuggestionsSize());
        assertEquals("åboulevarden", addressFilter.getMatchedAddress().getStreet());
        assertEquals("875", addressFilter.getMatchedAddress().getHouseNumber());
        assertEquals(4296, addressFilter.getMatchedAddress().getPostCode());
        assertEquals("nyrup", addressFilter.getMatchedAddress().getCity());

        //Prefix 3: Ø
        search(prefix3);
        assertNotNull(addressFilter.getMatchedAddress());
        assertEquals(1, getSuggestionsSize());
        assertEquals("østbanevej", addressFilter.getMatchedAddress().getStreet());
        assertEquals("8", addressFilter.getMatchedAddress().getHouseNumber());
        assertEquals(5600, addressFilter.getMatchedAddress().getPostCode());
        assertEquals("faaborg", addressFilter.getMatchedAddress().getCity());
    }

    //COMPLETE ADDRESS SEARCH MATCHING
    @Test
    public void testAddressSearchReturnsCorrectAddress() {
        String prefix = "Dwight-Robinsonvej 55H, 2640 Hedehusene";
        search(prefix);
        Address match = addressFilter.getMatchedAddress();
        assertNotNull(match);
        assertEquals(1, getSuggestionsSize());
        assertEquals("dwight-robinsonvej", match.getStreet());
        assertEquals("55h", match.getHouseNumber());
        assertEquals(2640, match.getPostCode());
        assertEquals("hedehusene", match.getCity());
        assertEquals(13, match.getNode().getId());
        assertEquals(13, match.getNode().getxMax());
        assertEquals(13, match.getNode().getyMax());
        assertEquals("dwight-robinsonvej 55h, 2640 hedehusene", match.toString());
    }

    @Test
    public void testAddressSearchReturnsCorrectAddressMultiMatches() {
        String prefix = "Studiestræde 18D, 1455 København K";
        search(prefix);
        Address match = addressFilter.getMatchedAddress();
        assertNotNull(match);
        assertEquals(1, getSuggestionsSize());
        assertEquals("studiestræde", match.getStreet());
        assertEquals("18d", match.getHouseNumber());
        assertEquals(1455, match.getPostCode());
        assertEquals("københavn k", match.getCity());
        assertEquals(38, match.getNode().getId());
        assertEquals(38, match.getNode().getxMax());
        assertEquals(38, match.getNode().getyMax());
        assertEquals("studiestræde 18d, 1455 københavn k", match.toString());
    }

    private void search(String prefix) {
        addressFilter.search(prefix);
    }

    private List<String> getSuggestions() {
        return addressFilter.getSuggestions();
    }

    private int getSuggestionsSize() {
        return addressFilter.getSuggestions().size();
    }

    @AfterAll
    public static void tearDown() {
        addressFilter = null;
        addressTriesTree = null;
        AddressTriesTree.POSTCODE_TO_CITIES.clear();
    }
}
