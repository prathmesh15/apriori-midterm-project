/**
 * Code is used to generate frequent itemsets and association rules.
 * Output of this code is a text file where all frequent itemsets and association rules are present.
 * Output is also printed on the console.
 */
package njit.datamining.apriori_midterm_project;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author prathmesh.pethkar
 *
 */
public class AprioriAlgorithm {

	static List<Set<String>> itemsetData;
	static double support;
	static double inputConfidence;
	static long totalTransactionsCount;
	static BufferedWriter bufferedWriter;

	public static void main(String[] args) throws IOException {

		if (validateInputArguments(args)) {
			Connection conn = DatabaseConnection.getConnection();
			Statement stmt = null;
			try {
				stmt = conn.createStatement();
				String sql;
				sql = "SELECT id,Milk,Cheese,Lays,Coke,Bagel,Sugar,Mayonise,Bread,Eggs,Deodrant FROM transactions";
				ResultSet rs = stmt.executeQuery(sql);

				List<Set<String>> itemsets = populateItemsetsFromResultSet(rs);

				String filename="C:\\Users\\prathmesh.pethkar\\Desktop\\Data mining final\\abc.txt";
				
				totalTransactionsCount = rs.last() ? rs.getRow() : 0;
				// System.out.println(totalTransactionsCount);

				bufferedWriter = new BufferedWriter(new FileWriter("Ouput.txt"));

				Map<Set<String>, Integer> itemsetCountInfo = new HashMap<>();

				bufferedWriter.write("***************Frequent Itemsets with support value***************\n\n\n\n");
				bufferedWriter.newLine();
				bufferedWriter.newLine();
				bufferedWriter.newLine();
				bufferedWriter.newLine();
				System.out.println("***************Frequent Itemsets with support value***************\n\n\n\n");

				Map<Set<String>, Integer> updatedItemCountSet = updateItemSetCount(itemsets, null);
				itemsetCountInfo.putAll(updatedItemCountSet);

				itemsetData.addAll(new ArrayList<>(itemsetCountInfo.keySet()));

				while (itemsetData.size() > 0) {

					Set<Set<String>> keySet = calculateNextItemSetsFromPreviousOnes(itemsetData);
					Map<Set<String>, Integer> itemSetCount = updateItemSetCount(itemsets, keySet);
					itemsetCountInfo.putAll(itemSetCount);
					if (itemSetCount.size() > 0) {
						List<Set<String>> otherKeySet = new ArrayList<>();
						otherKeySet.addAll(itemSetCount.keySet());
					}
				}

				generateAssociationRules(itemsetCountInfo);

				rs.close();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				DatabaseConnection.close(stmt, conn);
				if (bufferedWriter != null) {
					bufferedWriter.close();
				}
			}
		}
	}

	/**
	 * This method is used to validate the arguments like support and confidence
	 * 
	 * @param args
	 * @return Boolean : whether arguments are correct.
	 */
	private static Boolean validateInputArguments(String[] args) {
		boolean isValid = true;
		double d;
		double d1;
		if (args.length == 0 || args[0] == null || args[1] == null) {
			System.out.println("Arguments like support or confidence is missing");
			isValid = false;
		} else {
			try {
				d = Double.parseDouble(args[0]);
				d1 = Double.parseDouble(args[1]);
				if (d > 1.0 || d1 > 1.0) {
					System.out.println("Support or confidence cannot be greater than 100%");
					isValid = false;
				} else {
					support = d;
					inputConfidence = d1;
				}
			} catch (NumberFormatException nfe) {
				System.out.println("Arguments like support or confidence should be number");
				isValid = false;
			}
		}
		return isValid;

	}

	/**
	 * This method is used to generate association rules
	 * 
	 * @param itemsetCount
	 * @throws IOException
	 */
	private static void generateAssociationRules(Map<Set<String>, Integer> itemsetCount) throws IOException {
		Set<Set<String>> keySet = itemsetCount.keySet();
		// System.out.println("KeySet>>>>" + keySet);
		bufferedWriter.newLine();
		bufferedWriter.newLine();
		bufferedWriter.write("\n\n\n*************Association Rules with confidence***************************\n\n\n\n");
		bufferedWriter.newLine();
		bufferedWriter.newLine();
		bufferedWriter.newLine();
		bufferedWriter.newLine();
		System.out.println("\n\n\n*************Association Rules with confidence***************************\n\n\n\n");
		List<String> duplicateCheck = new ArrayList<>();

		for (Set<String> seta : keySet) {
			Set<String> set1 = new HashSet<>(seta);
			for (Set<String> setb : keySet) {
				Set<String> set2 = new HashSet<>(setb);
				set2.removeAll(set1);
				if (!duplicateCheck.contains(set1 + "==>" + set2) && itemsetCount.containsKey(set2)) {
					duplicateCheck.add(set1 + "==>" + set2);
					Set<String> allItems = new HashSet<>();
					allItems.addAll(set1);
					allItems.addAll(set2);
					if (itemsetCount.containsKey(allItems)) {
						double confidence = itemsetCount.get(allItems) * 1.00 / itemsetCount.get(set1);
						if (inputConfidence <= confidence) {
							bufferedWriter.write("Association Rule " + set1 + "==>" + set2 + " has confidence of "
									+ confidence + "\n");
							bufferedWriter.newLine();
							System.out.println("Association Rule " + set1 + "==>" + set2 + " has confidence of "
							+ confidence + "\n");

						}
					}
				}
			}
		}
	}

	/**
	 * This method is used to updateItemsetCount.
	 * 
	 * @param itemsets
	 * @param keySet
	 * @return
	 * @throws IOException
	 */
	private static Map<Set<String>, Integer> updateItemSetCount(List<Set<String>> itemsets, Set<Set<String>> keySet)
			throws IOException {

		Map<Set<String>, Integer> itemsetCount = new HashMap<>();

		if (keySet == null) {
			itemsetData = new ArrayList<>();
		}
		for (Set<String> list : itemsets) {
			if (keySet == null) {
				for (String string : list) {
					Set<String> items = new HashSet<>();
					items.add(string);

					if (itemsetCount.containsKey(items)) {
						int count = itemsetCount.get(items);
						itemsetCount.put(items, count + 1);
					} else {
						itemsetCount.put(items, 1);
					}
				}
			} else {
				for (Set<String> secondaryFrequencySet : keySet) {
					if (list.containsAll(secondaryFrequencySet)) {
						if (itemsetCount.containsKey(secondaryFrequencySet)) {
							int count = itemsetCount.get(secondaryFrequencySet);
							itemsetCount.put(secondaryFrequencySet, count + 1);
						} else {
							itemsetCount.put(secondaryFrequencySet, 1);
						}
					}
				}
			}
		}
		filterItemsetBySupport(itemsetCount);
		return itemsetCount;
	}

	/**
	 * This method is used to filter the itemsets by support value provided.
	 * 
	 * @param itemsetCount
	 * @throws IOException
	 */
	private static void filterItemsetBySupport(Map<Set<String>, Integer> itemsetCount) throws IOException {
		Set<Set<String>> unSupportedItemsets = new HashSet<>();

		for (Set<String> set : itemsetCount.keySet()) {
			Integer count = itemsetCount.get(set);
			Double d = (double) (count * 1.00 / totalTransactionsCount);
			if (!(support <= d)) {
				unSupportedItemsets.add(set);
			} else {
				bufferedWriter.write("Itemset " + set + " has the support of " + d + "\n");
				bufferedWriter.newLine();
				System.out.println("Itemset " + set + " has the support of " + d + "\n");
			}
		}
		for (Set<String> set : unSupportedItemsets) {
			itemsetCount.remove(set);
		}
	}

	/**
	 * This method is used to calculate next itemsets from existing ones.
	 * 
	 * @param previousItemSets
	 * @return
	 */
	private static Set<Set<String>> calculateNextItemSetsFromPreviousOnes(List<Set<String>> previousItemSets) {
		Set<Set<String>> newItemSets = new HashSet<>();

		for (int i = 0; i < previousItemSets.size(); i++) {
			for (int j = i + 1; j < previousItemSets.size(); j++) {
				Set<String> temp = new HashSet<>();
				Set<String> set1 = previousItemSets.get(i);
				Set<String> set2 = previousItemSets.get(j);
				temp.addAll(set1);
				temp.addAll(set2);
				if (temp.size() > 0 && !newItemSets.contains(temp)) {
					if (temp.size() <= set1.size() + 1)
						newItemSets.add(temp);
				}
			}
		}
		if (newItemSets.size() > 0) {
			itemsetData = new ArrayList<>();
			itemsetData.addAll(newItemSets);
		} else {
			itemsetData = new ArrayList<>();
		}
		return newItemSets;
	}

	/**
	 * This method is used to populate itemsets from resultset.
	 * 
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	private static List<Set<String>> populateItemsetsFromResultSet(ResultSet rs) throws SQLException {

		List<Set<String>> itemsets = new ArrayList<>();

		while (rs.next()) {
			Set<String> items = new HashSet<>();

			if (rs.getBoolean("Milk")) {
				items.add("Milk");
			}
			if (rs.getBoolean("Cheese")) {
				items.add("Cheese");
			}
			if (rs.getBoolean("Lays")) {
				items.add("Lays");
			}
			if (rs.getBoolean("Coke")) {
				items.add("Coke");
			}
			if (rs.getBoolean("Bagel")) {
				items.add("Bagel");
			}
			if (rs.getBoolean("Sugar")) {
				items.add("Sugar");
			}
			if (rs.getBoolean("Eggs")) {
				items.add("Eggs");
			}
			if (rs.getBoolean("Mayonise")) {
				items.add("Mayonise");
			}
			if (rs.getBoolean("Deodrant")) {
				items.add("Deodrant");
			}
			if (rs.getBoolean("Bread")) {
				items.add("Bread");
			}
			itemsets.add(items);

		}
		return itemsets;
	}
}