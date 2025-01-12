import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class TagAggregator {
    public static final String LOOKUP_PATH = "../lookup.csv";
    public static final String FLOW_DATA_PATH = "../flowdata.txt";
    public static final String OUTPUT_PATH = "../outputFile.txt";

    public static Map<String, String> protocols = new HashMap<>();

    public static void main(String[] args) {
        // extract lookup table, create mapping from dstport+protocol to tag
        Map<String, String> protocolMapping = lookupMapping(LOOKUP_PATH);
        Map<String, Integer> pCounts = new HashMap<>();
        Map<String, Integer> tagCount = new HashMap<>();
        // adding protocol mapping
        protocols.put("1", "icmp"); protocols.put("6", "tcp"); protocols.put("17", "udp");
        protocols.put("2", "igmp"); protocols.put("3", "ggp"); protocols.put("4", "ipip");
        protocols.put("50", "esp"); protocols.put("51", "ah"); protocols.put("88", "eigrp");
        protocols.put("89", "ospf"); protocols.put("115", "pptp"); protocols.put("132", "sctp");
        protocols.put("41", "ipv6"); protocols.put("64", "aodv"); protocols.put("9", "rip");

        // read rows, and get count information to tagCount
        Combine cur;
        String tag;
        try (BufferedReader br = new BufferedReader(new FileReader(FLOW_DATA_PATH))) {
            String line;
            // Read each line
            while ((line = br.readLine()) != null) {
                cur = getCom(line);
                tag = "Untagged";

                if (protocolMapping.containsKey(cur.dstport + cur.protocol)) {
                    tag = protocolMapping.get(cur.dstport + cur.protocol);
                }
                cur.tag = tag;
                String pKey = cur.dstport+","+cur.protocol;
                pCounts.put(pKey, pCounts.getOrDefault(pKey, 0) + 1);
                tagCount.put(tag, tagCount.getOrDefault(tag, 0) + 1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        writeToFile(OUTPUT_PATH, pCounts, tagCount);
    }
    private static Combine getCom(String line) {
        String[] strs = line.toLowerCase().split(" ");
        String protocol = protocols.get(strs[8]);
        String port = strs[6];
        return new Combine(port, protocol);
    }
    private static Map<String, String> lookupMapping(String filePath) {
        Map<String, String> protocolMapping = new HashMap<>();
        String delimiter = ",";
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            // Read each line
            while ((line = br.readLine()) != null) {
                String[] columns = line.split(delimiter);
                if(columns.length == 0) continue;
                String dstport = columns[0];
                String protocol = columns[1];
                String tag = columns[2];
                protocolMapping.put(dstport + protocol, tag);
            }
        } catch (IOException e) {
            e.printStackTrace();  // Handle exceptions
        }
        return protocolMapping;
    }
    private static void writeToFile(String outputFile, Map<String, Integer> pCounts, Map<String, Integer> tagCount) {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, true))) {
            // Count of matches for each tag
            writer.write("Tag Counts:");
            writer.newLine();
            writer.write("Tag" + "," + "Count");
            writer.newLine();

            for (Map.Entry<String, Integer> entry: tagCount.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue());
                writer.newLine();
            }
            // Count of matches for each port/protocol combination
            writer.write("Port/Protocol Combination Counts:");
            writer.newLine();
            writer.write("Port" + "," + "Protocol" + "Count");
            writer.newLine();
            for (Map.Entry<String, Integer> entry: pCounts.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();  // Handle potential IO exceptions
        }
    }
}

