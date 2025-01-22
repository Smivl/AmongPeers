package Server;

import Game.GameCharacter.CharacterType;
import Game.Interactables.Sabotage.SabotageType;
import Game.Interactables.Task.Task;
import Game.Interactables.Task.TaskType;
import Game.Player.PlayerInfo;
import org.jspace.*;

import java.net.URI;
import java.util.*;
import java.util.function.Function;


public class Server {
    private static final int MAX_PLAYERS = 8;
    private static final List<double[]> SPAWN_POINTS = new ArrayList<>() {{
        add(new double[]{4621, 1061});
        add(new double[]{4855, 1001});
        add(new double[]{5070, 1071});
        add(new double[]{5166, 1231});
        add(new double[]{5089, 1418});
        add(new double[]{4858, 1503});
        add(new double[]{4613, 1440});
        add(new double[]{4529, 1249});
    }};

    private static final List<TaskType> TASK_LIST = new ArrayList<>(List.of(TaskType.values()));
    private static final int TASKS_PER_PERSON = 3;

    private int tasksCompleted = 0;
    private int totalTasks = 0;

    private int playersAlive;

    private boolean sabotageActive = false;
    private SabotageType sabotageType = null;

    private Map<String, Integer> playerVotes;
    private Map<String, Space> playerSpaces = new HashMap<>();
    private Map<String, Thread> playerThreads = new HashMap<>();
    private Map<String, PlayerInfo> playerInfos = new HashMap<>();

    private Space sabotageSpace;

    private SpaceRepository spaceRepository = new SpaceRepository();
    private final Space serverSpace;
    private final Space join_start_lock = new SequentialSpace();
    private final URI serverURI;
    private ServerState state = ServerState.LOBBY_STATE;

    private Thread serverThread;
    private Thread meetingThread;
    private Thread sabotageThread;

    public Server(URI uri, Space serverSpace){
        this.serverURI = uri;
        this.serverSpace = serverSpace;
        spaceRepository.add("server", serverSpace);
        spaceRepository.addGate(serverURI);
    }

    public void start(){
        try {
            join_start_lock.put("TOKEN");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        serverThread = new Thread(this::server);
        serverThread.start();

        System.out.println("Server now online:\naddress:" + serverURI);
    }

    public void shutdown(){
        spaceRepository.closeGate(serverURI);

        if(serverThread.isAlive() && !serverThread.isInterrupted()) serverThread.interrupt();
        try{
            serverThread.join();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

        System.out.println("Server offline.");
    }

    /*
    * Server loop: takes care of lobby requests
    * */
    private void server(){
        while (true){
            try {
                Object[] request = serverSpace.get(new FormalField(Request.class));
                switch ((Request) request[0]){
                    case JOIN: {
                        handleJoinRequest();
                        break;
                    }
                    case LEAVE: {
                        handleLeaveRequest();
                        break;
                    }
                    case KICK: {
                        System.out.println("Unsupported request!");
                        break;
                    }
                }
            } catch (Exception e){
                System.out.println(e.getMessage());
                break;
            }
        }
    }

    // inform players of game start
    public void startGame() {

        try {
            join_start_lock.get(new ActualField("TOKEN"));
            state = ServerState.RUNNING_STATE;

            // DETERMINE IMPOSTER AND PLAYER SPAWN POINTS
            Collections.shuffle(SPAWN_POINTS);
            Random random = new Random();

            int imposterIndex = random.nextInt(playerSpaces.size());
            int playerNumber = 0;

            for (Map.Entry<String, Space> playerSpaceEntry : playerSpaces.entrySet()){
                try {
                    // generate set of tasks
                    Collections.shuffle(TASK_LIST);
                    List<TaskType> playerTasks = TASK_LIST.subList(0, TASKS_PER_PERSON);

                    if(playerNumber != imposterIndex){
                        totalTasks += TASKS_PER_PERSON;
                    }

                    initializePlayer(
                            playerSpaceEntry.getKey(),
                            CharacterType.RED, SPAWN_POINTS.get(playerNumber),
                            playerNumber == imposterIndex,
                            playerTasks.toArray(new TaskType[0])
                    );

                    playerSpaceEntry.getValue().put(ServerUpdate.GAME_START);
                    playerNumber++;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            join_start_lock.put("TOKEN");

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private void handleJoinRequest() {
        try {
            // read request
            String nameRequest = (String)(serverSpace.get(new ActualField(Request.JOIN), new FormalField(String.class))[1]);
            join_start_lock.get(new ActualField("TOKEN"));

            if (playerSpaces.containsKey(nameRequest)){
                // player name exists already
                serverSpace.put(nameRequest, Response.CONFLICT);
            }
            else if (!(playerSpaces.size()<MAX_PLAYERS)){
                // Too many people
                serverSpace.put(nameRequest, Response.FULL_ROOM);
            } else if (state != ServerState.LOBBY_STATE) {
                // game has started already
                serverSpace.put(nameRequest, Response.PERMISSION_DENIED);
            } else {
                // player name does not exist

                // create new channel
                Space privateChannel = new SequentialSpace();
                playerSpaces.put(nameRequest, privateChannel);
                spaceRepository.add(nameRequest, privateChannel);

                Thread playerThread = new Thread(() -> handlePlayer(nameRequest));
                playerThreads.put(nameRequest, playerThread);
                playerThread.start();

                // accept request
                serverSpace.put(nameRequest, Response.ACCEPTED);

            }
            join_start_lock.put("TOKEN");
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

    private void handleLeaveRequest(){
        try {
            String name = (String)(serverSpace.get(new ActualField(Request.LEAVE), new FormalField(String.class))[1]);
            broadcastClientUpdateExcludingSender(ServerUpdate.PLAYER_LEFT, name);

            playerThreads.get(name).interrupt();
            playerThreads.remove(name);

            playerSpaces.remove(name);
            playerInfos.remove(name);

        }catch (Exception e){
            System.out.println("error in handle leave");
        }
    }

    private void initializePlayer(String nameRequest, CharacterType color,double[] spawnPoint, boolean isImposter, TaskType[] playerTasks){
        try{

            PlayerInfo newPlayerInfo = new PlayerInfo(color, spawnPoint, new double[]{0,0}, true, isImposter);

            broadcastClientUpdateExcludingSender(ServerUpdate.PLAYER_JOINED, nameRequest, newPlayerInfo);

            playerSpaces.get(nameRequest).put(ServerUpdate.PLAYER_INIT, newPlayerInfo, playerTasks);
            playerInfos.put(nameRequest, newPlayerInfo);

        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    /*
    * player loop: takes case of player actions. One thread for each player
    * */
    private void handlePlayer(String playerName) {
        while (true){
            try{
                Object[] updateTuple = playerSpaces.get(playerName).get(new FormalField(ClientUpdate.class));
                ClientUpdate update = (ClientUpdate) updateTuple[0];

                switch (update){
                    case POSITION:{
                        switch (state){
                            case RUNNING_STATE:{
                                Object[] infoTuple = playerSpaces.get(playerName).get(new ActualField(ClientUpdate.POSITION), new FormalField(Object.class), new FormalField(Object.class));

                                playerInfos.get(playerName).position = (double[]) infoTuple[1];
                                playerInfos.get(playerName).velocity = (double[]) infoTuple[2];

                                broadcastClientUpdateExcludingSender(ServerUpdate.POSITION, playerName,
                                        playerInfos.get(playerName).position,
                                        playerInfos.get(playerName).velocity
                                );
                                break;
                            }
                            case MEETING_STATE:
                                ignoreUpdate(update, playerName);
                                break;
                        }
                        break;
                    }
                    case KILL:{
                        switch (state){
                            case RUNNING_STATE:{
                                Object[] infoTuple = playerSpaces.get(playerName).get(new ActualField(ClientUpdate.KILL), new FormalField(String.class) ,new FormalField(String.class));

                                String killerPlayerName = (String) infoTuple[1];
                                if (playerInfos.get(killerPlayerName).isImposter){
                                    String killedPlayerName = (String) infoTuple[2];

                                    playerInfos.get(killedPlayerName).isAlive = false;
                                    broadCastClientUpdateIncludingSender(ServerUpdate.KILLED, killedPlayerName);

                                    if(checkIfImposterWins()){
                                        broadCastClientUpdateIncludingSender(ServerUpdate.IMPOSTERS_WIN, killerPlayerName);
                                    }
                                }

                                break;
                            }
                            case MEETING_STATE:{
                                ignoreUpdate(update, playerName);
                                break;
                            }
                        }
                        break;
                    }
                    case REPORT:{
                        switch (state){
                            case RUNNING_STATE:{
                                if(sabotageActive){
                                    this.sabotageActive = false;
                                    this.sabotageType = null;
                                    broadCastClientUpdateIncludingSender(ServerUpdate.SABOTAGE_ENDED, playerName);
                                }
                                callMeeting(playerName);
                                break;
                            }
                            case MEETING_STATE:
                            case LOBBY_STATE:{
                                ignoreUpdate(update, playerName);
                                break;
                            }
                        }
                    }
                    case MEETING:{
                        switch (state){
                            case RUNNING_STATE:
                            {
                                callMeeting(playerName);
                                break;
                            }
                            case MEETING_STATE: {
                                break;
                            }
                        }
                        break;
                    }
                    case MESSAGE:{
                        switch (state){
                            case RUNNING_STATE:
                            {
                                ignoreUpdate(update, playerName);
                                break;
                            }
                            case MEETING_STATE: {
                                Object[] infoTuple = playerSpaces.get(playerName).get(new ActualField(ClientUpdate.MESSAGE), new FormalField(String.class));
                                broadCastClientUpdateIncludingSender(ServerUpdate.MESSAGE, playerName, infoTuple[1]);
                                break;
                            }
                        }
                        break;
                    }
                    case VOTE:{
                        switch (state){
                            case MEETING_STATE: {
                                Object[] infoTuple = playerSpaces.get(playerName).get(new ActualField(ClientUpdate.VOTE), new FormalField(String.class));
                                String voted = (String) infoTuple[1];

                                // add 1 to the votes received from voter
                                playerVotes.put(voted, playerVotes.getOrDefault(voted, 0) + 1);;


                                // NO NEED for it
                                //broadCastClientUpdateIncludingSender(ServerUpdate.VOTE, playerName, voted);
                                break;
                            }
                            case RUNNING_STATE: {
                                ignoreUpdate(update, playerName);
                                break;
                            }
                        }
                        break;
                    }
                    case TASK_COMPLETE:{
                        tasksCompleted++;

                        Object[] infoTuple = playerSpaces.get(playerName).get(new ActualField(ClientUpdate.TASK_COMPLETE), new FormalField(String.class));
                        String name = (String) infoTuple[1];
                        System.out.println(tasksCompleted/totalTasks);
                        broadCastClientUpdateIncludingSender(ServerUpdate.TASK_COMPLETE, name, (double) tasksCompleted/totalTasks);

                        if(tasksCompleted == totalTasks){
                            broadCastClientUpdateIncludingSender(ServerUpdate.CREWMATES_WIN, name);
                        }
                        break;
                    }
                    case SABOTAGE:{
                        switch (state){
                            case RUNNING_STATE:{
                                Object[] infoTuple = playerSpaces.get(playerName).get(new ActualField(ClientUpdate.SABOTAGE), new FormalField(String.class), new FormalField(SabotageType.class));

                                this.sabotageActive = true;
                                this.sabotageType = (SabotageType) infoTuple[2];

                                callSabotage(playerName, sabotageType);
                                broadCastClientUpdateIncludingSender(ServerUpdate.SABOTAGE_STARTED, (String) infoTuple[1], infoTuple[2]);

                                break;
                            }
                        }
                    }
                }
            }catch (Exception e){
                System.out.println(e.getMessage());
                break;
            }
        }
    }


    // helper functions

    private String votedPlayer() {
        String winner = null;
        int maxVotes = -1;
        boolean tie = true;

        for (Map.Entry<String, Integer> entry : playerVotes.entrySet()) {
            int voteCount = entry.getValue();

            if (voteCount > maxVotes) {
                // Found a strictly bigger vote count, update winner
                maxVotes = voteCount;
                winner = entry.getKey();
                tie = false; // reset tie flag
            } else if (voteCount == maxVotes) {
                // Found another with the same max votes -> it's a tie
                tie = true;
            }
        }

        // If tie is true, return null; otherwise return the winner's name.
        return tie ? "NO_ELIMINATION" : winner;
    }

    private void callSabotage(String playerName, SabotageType type){

        sabotageSpace = new SequentialSpace();
        spaceRepository.add("sabotage", sabotageSpace);

        try {
            sabotageSpace.put("lock");
        }catch (Exception e){
            System.out.println(e.getStackTrace());
        }

        sabotageThread = (new Thread(() -> {
            while (sabotageActive) {
                try {
                    switch (type) {
                        case NUCLEAR_MELTDOWN: {

                            if(sabotageSpace.queryp(new ActualField("reactor1")) != null && sabotageSpace.queryp(new ActualField("reactor2")) != null){
                                endSabotage(playerName);
                            }

                            break;
                        }
                        case LIGHTS: {

                            sabotageSpace.get(new ActualField("lights"));
                            endSabotage(playerName);

                            break;
                        }
                        case OXYGEN_DEPLETED: {

                            sabotageSpace.get(new ActualField("p1"));
                            sabotageSpace.get(new ActualField("p2"));
                            endSabotage(playerName);

                            break;
                        }
                    }

                } catch (InterruptedException e) {
                    System.out.println("Sabotage thread stopped");
                    spaceRepository.remove("sabotage");
                }
            }
        }));

        sabotageThread.start();
    }

    private void endSabotage(String playerName){
        sabotageActive = false;
        sabotageType = null;

        broadCastClientUpdateIncludingSender(ServerUpdate.SABOTAGE_ENDED, playerName);
        if(sabotageThread != null) sabotageThread.interrupt();
        spaceRepository.remove("sabotage");
    }

    private void callMeeting(String playerName){
        // change state
        state = ServerState.MEETING_STATE;

        // initialize empty votes
        playerVotes = new HashMap<>();

        // meeting logic
        meetingThread = (new Thread(() -> {
            try {
                // timer 1 minute
                Thread.sleep(30*1000);
                /*
                 * Tell the player who was eliminated: if there is a tie, it is "NO_ELIMINATION"
                 * */
                state = ServerState.RUNNING_STATE;

                String eliminatedPlayer = this.votedPlayer();
                broadCastClientUpdateIncludingSender(ServerUpdate.MEETING_DONE, eliminatedPlayer);

                // check for game end
                playerInfos.get(eliminatedPlayer).isAlive = false;
                if(checkIfCrewmateWins()){
                    broadCastClientUpdateIncludingSender(ServerUpdate.CREWMATES_WIN, eliminatedPlayer);
                }
                if(checkIfImposterWins()){
                    broadCastClientUpdateIncludingSender(ServerUpdate.IMPOSTERS_WIN, eliminatedPlayer);
                }

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }));
        meetingThread.start();

        broadCastClientUpdateIncludingSender(ServerUpdate.MEETING_START, playerName);
    }

    private boolean checkIfCrewmateWins(){
        int impostersAlive = 0;
        for(PlayerInfo player: playerInfos.values()){
            if (player.isImposter && player.isAlive) impostersAlive++;
        }
        return impostersAlive == 0;
    }

    private boolean checkIfImposterWins(){
        int crewmatesAlive = 0;
        for(PlayerInfo player: playerInfos.values()){
            if (!player.isImposter && player.isAlive) crewmatesAlive++;
        }
        return crewmatesAlive < 2;
    }
    /*
    * broadcasts an update from the server to every player.
    * */
    private void broadcastClientUpdateExcludingSender(ServerUpdate updateCode, String playerName, Object... toBroadcast){
        broadcastClientUpdateGivenCondition((String s) -> !s.equals(playerName), updateCode, playerName, toBroadcast);
    }

    private void broadCastClientUpdateIncludingSender(ServerUpdate updateCode, String playerName, Object... toBroadcast){
        broadcastClientUpdateGivenCondition(((String s) -> true), updateCode, playerName, toBroadcast);
    }
    /*
    * cleans up after an update that is out of context
    * */
    private void ignoreUpdate(ClientUpdate update, String playerName){
        try
        {
            switch (update){
                case POSITION: {
                    playerSpaces.get(playerName).get(new ActualField(ClientUpdate.POSITION),new FormalField(Object.class), new FormalField(Object.class));
                    break;
                }
                case VOTE: {
                    playerSpaces.get(playerName).get(new ActualField(ClientUpdate.VOTE), new FormalField(String.class));
                    break;
                }
                case MEETING:{
                    break;
                }
                case MESSAGE:{
                    playerSpaces.get(playerName).get(new ActualField(ClientUpdate.MESSAGE), new FormalField(String.class), new FormalField(String.class));
                }
                case KILL:{
                    break;
                }

            }
        } catch (Exception e){
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }

    private void broadcastClientUpdateGivenCondition(Function<String, Boolean> condition, ServerUpdate updateCode, String playerName, Object... toBroadcast){
        try {
            switch (updateCode){
                case POSITION: { // two field cases
                    for(String name : playerSpaces.keySet()){
                        if (condition.apply(name)){
                            playerSpaces.get(name).put(updateCode);
                            playerSpaces.get(name).put(updateCode, playerName, toBroadcast[0], toBroadcast[1]);
                        }
                    }
                    break;
                }
                case SABOTAGE_STARTED:
                case TASK_COMPLETE:
                case VOTE:
                case MESSAGE:
                case PLAYER_JOINED: { // one field cases
                    for(String name : playerSpaces.keySet()){
                        if (condition.apply(name)){
                        playerSpaces.get(name).put(updateCode);
                        playerSpaces.get(name).put(updateCode, playerName, toBroadcast[0]);
                        }
                    }
                    break;
                }
                case SABOTAGE_ENDED:
                case KILLED: // playerName is the name to kill
                case PLAYER_LEFT: // zero field cases
                case CREWMATES_WIN:
                case IMPOSTERS_WIN:
                case MEETING_START: // meeting is called by one player (playerName field)
                case MEETING_DONE: { // meeting is ended by server. playerName is the player to kick out
                    for(String name : playerSpaces.keySet()){
                        if (condition.apply(name)){
                        playerSpaces.get(name).put(updateCode);
                        playerSpaces.get(name).put(updateCode, playerName);
                        }
                    }
                    break;
                }
            }
        } catch (Exception e){
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }
}
