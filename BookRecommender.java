import java.io.*;
import java.util.*;
public class BookRecommender {
    //part1 and part3 use this together
    static Map<String, Set<String>> userToBooks = new HashMap<>();//The user to book
    static Map<String, Set<String>> bookToUsers = new HashMap<>();//Record the book to user
    static Map<String, Map<String, Integer>> graph = new HashMap<>();//Record the graph

    public static void main(String[] args) {
        try {
            if (args.length < 2) {//If fewer than 2 argument
                System.out.println("NONE");//Print none
                return;
            }
            loadData(args[0]);//load csv
            buildGraph();//build graph
            String cmd = args[1];//extra command
            if (cmd.equals("single_book_mn")) {//2a
                if (args.length >= 3) doSingleBook(args[2]);
                else System.out.println("NONE");
            } else if (cmd.equals("like_history_mn")) {//2b
                doLikeHistory(args);
            } else if (cmd.equals("user_cf")) {//part4
                if (args.length >= 3) doUserCF(args[2]);
                else System.out.println("NONE");
            } else if (cmd.equals("shortest_path")) {//part5
                if (args.length >= 4) doShortestPath(args[2], args[3]);
                else System.out.println("NONE");
            } else {
                System.out.println("NONE");//Not recognize
            }
        } catch (Exception e) {//If error
            System.out.println("NONE");
        }
    }

    //part1 read csv
    static void loadData(String file) throws Exception {
        BufferedReader ld = new BufferedReader(new FileReader(file));//Open file
        String line;//Store value
        while((line = ld.readLine()) != null){//Read line by line
            String[] parts = line.split(",");
            String user = parts[0];//User ID
            String book = parts[1];//Book ID
            userToBooks.putIfAbsent(user, new HashSet<>());//If user not exist，build new set
            userToBooks.get(user).add(book);//Add this book to set
            bookToUsers.putIfAbsent(book, new HashSet<>());//If user not in it, build new set
            bookToUsers.get(book).add(user);//Store in new set
        }
        ld.close();
    }

    //part1 build graph
    static void buildGraph(){
        for (String user : userToBooks.keySet()){//Walk every user and the book they like
            List<String> books = new ArrayList<>(userToBooks.get(user));//Get all the book
            for(int i = 0; i < books.size(); i++){//Take a book from all the book
                for(int j = i+1; j < books.size(); j++){//Take another book from all the book
                    String b1 = books.get(i);//get a book
                    String b2 = books.get(j);//get different book
                    graph.putIfAbsent(b1, new HashMap<>());//If not b1, create a graph
                    graph.putIfAbsent(b2, new HashMap<>());//Create graph for b2
                    graph.get(b1).put(b2, graph.get(b1).getOrDefault(b2, 0) + 1);//+1
                    graph.get(b2).put(b1, graph.get(b2).getOrDefault(b1, 0) + 1);//Add both side
                }
            }
        }
    }

    //part2a
    static void doSingleBook(String book){//Handle single book
        if(!graph.containsKey(book)){//IF the book not exist
            System.out.println("NONE");//Print none
            return;//Return
        }
        Map<String, Integer> map = graph.get(book);//Get the neighbor of this book
        List<String> list = new ArrayList<>(map.keySet());//Put in the list
        if (map.isEmpty()) {//If empty
            System.out.println("NONE");//print none
            return;
        }
        for (int i = 0; i < list.size()-1; i++){//Outer loop
            for (int j = 0; j < list.size()-1-i; j++){//Inner loop
                String bookA = list.get(j);//Get book on left
                String bookB = list.get(j+1);//Get book on right
                int weightA = map.get(bookA);//Check weight on left
                int weightB = map.get(bookB);//Check weight on right
                boolean needSwap = false;//Check whether need swap
                if(weightA < weightB){
                    needSwap = true;//If order wrong, swap
                }else if(weightA == weightB){//If same weight
                    if(bookA.compareTo(bookB) > 0){//Check letter
                        needSwap = true;
                    }
                }
                if(needSwap){
                    list.set(j, bookB);//left to right
                    list.set(j+1, bookA);//right to left
                }
            }
        }
        printTop5(list);//Print top 5 books
    }

    //part2b
    static void doLikeHistory(String[] args){
        Map<String, Integer> score = new HashMap<>();//Calcuate the score
        Set<String> input = new HashSet<>();//Record already input
        if (score.isEmpty()) {//If empty
            System.out.println("NONE");//print none
            return;
        }
        for (int i =2; i < args.length; i++){//Start third parameter
            input.add(args[i]);//Add the book already read
        }
        for(String book : input){//Walk every book that has been read
            if(!graph.containsKey(book)) continue;//If no neighbor, continue
            for(String neighbor : graph.get(book).keySet()){//Walk every neighbor's book
                if (input.contains(neighbor)) {//If neighbor also read
                    continue;//skip
                }
                int n = graph.get(book).get(neighbor);//Check weight
                score.put(neighbor, score.getOrDefault(neighbor, 0) + n);
            }
        }
        List<String> list = new ArrayList<>(score.keySet());//list all the score
        for (int i = 0; i < list.size()-1; i++) {
            for (int j = 0; j < list.size()-1-i; j++) {//compare book
                String bookA = list.get(j);//the first
                String bookB = list.get(j + 1);//the second
                int scoreA = score.get(bookA);//Check first book score
                int scoreB = score.get(bookB);//Check second book score
                boolean needSwap = false;//Check whether swap
                if (scoreA < scoreB) {
                    needSwap = true;//If order wrong, swap
                } else if (scoreA == scoreB && bookA.compareTo(bookB)>0) needSwap = true;//If same weight and bigger than 0
                if (needSwap) {
                    list.set(j, bookB);//left to right
                    list.set(j + 1, bookA);//right to left
                }
            }
        }
        printTop5(list);
    }

    //part4
    static void doUserCF(String target){
        Set<String> targetBooks = userToBooks.get(target);//Get all the book from users
        if (targetBooks == null||targetBooks.isEmpty()){//If user not exist or book is empty
            System.out.println("NONE");//Print none
            return;
        }
        Map<String, Double> similar = new HashMap<>();//Map store similar scores
        for (String user : userToBooks.keySet()){//All user
            if (user.equals(target)){//Skip target user
                continue;
            }
            Set<String> other = userToBooks.get(user);//Get the books like by other user
            Set<String> intersection = new HashSet<>(targetBooks);
            intersection.retainAll(other);//Keep common book
            if (intersection.size()==0)continue;//If not common, skip
            Set<String> union = new HashSet<>(targetBooks);//Calculate union
            union.addAll(other);//Overlap books that read
            double score = (double) intersection.size()/union.size();//Jaccard formula
            similar.put(user, score);//Store similar score
        }
        List<String> users = new ArrayList<>(similar.keySet());//Grab all name
        for (int i = 0; i < users.size()-1; i++) {
            for (int j = 0; j < users.size()-1-i; j++) {//compare user
                String userA = users.get(j);//the first
                String userB = users.get(j + 1);//the second
                double scoreA = similar.get(userA);//Check first book score
                double scoreB = similar.get(userB);//Check second book score
                boolean needSwap = false;//Check whether swap
                if (scoreA < scoreB) {
                    needSwap = true;//If order wrong, swap
                } else if (scoreA == scoreB) {//If same weight
                    if (userA.compareTo(userB) > 0) {//Check letter
                        needSwap = true;
                    }
                }
                if (needSwap) {
                    users.set(j, userB);//left to right
                    users.set(j + 1, userA);//right to left
                }
            }
        }
        users = users.subList(0, Math.min(5, users.size())); //Keep top 5
        Map<String, Double> bookScore = new HashMap<>(); //Map to store final scores
        for (String u : users) { //Walk through the top 5 twins
            for (String b : userToBooks.get(u)) { //walk through each book
                if (targetBooks.contains(b)) continue; // Skip if target already read
                double score = 1.0 / bookToUsers.get(b).size(); //Add 1
                bookScore.put(b, bookScore.getOrDefault(b, 0.0) + score); // Accumulate the  score
            }
        }
        List<String> books = new ArrayList<>(bookScore.keySet());//Extract ID for sorting
        if (bookScore.isEmpty()) {//If empty
            System.out.println("NONE");//Print none
            return;
        }
        for (int i = 0; i < books.size() - 1; i++) {  //Outer loop controls the number of passes
            for (int j = 0; j < books.size() - 1 - i; j++) {//compares adjacent elements
                String bookA = books.get(j);// Get left book ID
                String bookB = books.get(j + 1);// Get right book ID
                double scoreA = bookScore.get(bookA);//Get score of left book
                double scoreB = bookScore.get(bookB);//Get score of right book
                boolean needSwap = false; //Check whether swap
                if (scoreA < scoreB) {//Lower score moves back
                    needSwap = true; //Swap
                }
                else if (scoreA == scoreB) { //If scores equal
                    if (bookA.compareTo(bookB) > 0) { //Check letter
                        needSwap = true;//Swap
                    }
                }
                if (needSwap) { //If swap
                    books.set(j, bookB); // Put bookB in current position
                    books.set(j + 1, bookA); //Put bookA in next position
                }
            }
        }
        printTop5(books);
    }

    //part5
    static void doShortestPath(String start, String end) {//Check shortest path
        if (!graph.containsKey(start) || !graph.containsKey(end)) {//If not start or ending
            System.out.println("NONE");//print none
            return;
        }
        List<Integer> weights = new ArrayList<>();//Collect all weight
        for (String b : graph.keySet()) {//Walk all the node
            for (int wei : graph.get(b).values()) {//Walk all edge
                weights.add(wei);//Add each weight
            }
        }
        if (weights.isEmpty()) {//If not edge
            System.out.println("NONE");//Print none
            return;
        }
        for (int i = 0; i < weights.size() - 1; i++) {
            for (int j = 0; j < weights.size() - 1 - i; j++) {//Compare elements
                if (weights.get(j) > weights.get(j + 1)) {//If current greater than next
                    int temp = weights.get(j); //Swap
                    weights.set(j, weights.get(j + 1));
                    weights.set(j + 1, temp);
                }
            }
        }
        int median = weights.get(weights.size() / 2);//Select median
        Queue<List<String>> q = new LinkedList<>();//BFS, create Queue
        Set<String> visited = new HashSet<>();//Avoid cycle
        q.add(new ArrayList<>(Arrays.asList(start)));//Initial queue
        visited.add(start);//Mark start as visited
        while (!q.isEmpty()) {//If not empty
            List<String> path = q.poll();//get current path from q
            String last = path.get(path.size() - 1);//Get last node

            if (last.equals(end)) {//If end
                System.out.println(String.join("->", path));//Print ->
                return;
            }
            List<String> neighbors = new ArrayList<>(graph.getOrDefault(last, new HashMap<>()).keySet());//get all the neighbor
            for (int i = 0; i < neighbors.size() - 1; i++) {//Walk all the neighbor
                for (int j = 0; j < neighbors.size() - 1 - i; j++) {//Sort the neighbor
                    if (neighbors.get(j).compareTo(neighbors.get(j + 1)) > 0) {
                        String temp = neighbors.get(j);//Store temperarily element
                        neighbors.set(j, neighbors.get(j + 1));//Move to next element
                        neighbors.set(j + 1, temp);//Place the elements
                    }
                }
            }
            for (String nei : neighbors) {//check each neighbor
                int wei = graph.get(last).get(nei);//compare current and neighbor
                if (wei < median) continue; //If smaller than median, skip
                if (!visited.contains(nei)) {//Check whether cycle
                    visited.add(nei);//make neighbor as visited
                    List<String> newPath = new ArrayList<>(path);//Create new path
                    newPath.add(nei);//Add to new path
                    q.add(newPath);//Add to q
                }
            }
        }
        System.out.println("NONE");//Print none
    }
    static void printTop5(List<String> list) {
        if(list.isEmpty()){//If size empty
            System.out.println("NONE");//Print none
            return;
        }
        int n = Math.min(5, list.size());//How many item print
        for (int i = 0; i < n; i++) {//Walk every element
            System.out.print(list.get(i));//Print current item
            if(i < n-1){//If not last item
                System.out.print(",");//print','
            }
        }
        System.out.println();//Print a newline
    }
}
