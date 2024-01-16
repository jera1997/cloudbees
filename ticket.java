import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@RestController
@RequestMapping("/train")
public class TrainBoardingApplication {

    private Map<String, TrainTicket> ticketMap = new HashMap<>();
    private Map<String, String> seatMap = new HashMap<>();
    private int seatCounter = 1;

    public static void main(String[] args) {
        SpringApplication.run(TrainBoardingApplication.class, args);
    }

    @PostMapping("/purchase")
    public ResponseEntity<String> purchaseTicket(@RequestBody TrainTicketRequest ticketRequest) {
        TrainTicket ticket = createTrainTicket(ticketRequest);
        String userId = generateUserId(ticket);
        allocateSeat(userId);
        ticketMap.put(userId, ticket);
        return ResponseEntity.ok("Ticket purchased successfully. Receipt:\n" + ticket.toString());
    }

    @GetMapping("/receipt/{userId}")
    public ResponseEntity<TrainTicket> getReceipt(@PathVariable String userId) {
        TrainTicket ticket = ticketMap.get(userId);
        if (ticket != null) {
            return ResponseEntity.ok(ticket);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/users/{section}")
    public ResponseEntity<Map<String, String>> getUsersBySection(@PathVariable String section) {
        Map<String, String> usersInSection = new HashMap<>();
        for (Map.Entry<String, String> entry : seatMap.entrySet()) {
            if (entry.getValue().equals(section)) {
                usersInSection.put(entry.getKey(), section);
            }
        }
        return ResponseEntity.ok(usersInSection);
    }

    @DeleteMapping("/remove/{userId}")
    public ResponseEntity<String> removeUser(@PathVariable String userId) {
        if (ticketMap.containsKey(userId)) {
            String section = seatMap.get(userId);
            ticketMap.remove(userId);
            seatMap.remove(userId);
            return ResponseEntity.ok("User removed from section " + section);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/modify/{userId}/{newSection}")
    public ResponseEntity<String> modifySeat(@PathVariable String userId, @PathVariable String newSection) {
        if (ticketMap.containsKey(userId) && (newSection.equals("A") || newSection.equals("B"))) {
            seatMap.put(userId, newSection);
            return ResponseEntity.ok("Seat modified to section " + newSection);
        } else {
            return ResponseEntity.badRequest().body("Invalid user or section");
        }
    }

    private TrainTicket createTrainTicket(TrainTicketRequest ticketRequest) {
        User user = new User(ticketRequest.getFirstName(), ticketRequest.getLastName(), ticketRequest.getEmail());
        return new TrainTicket("London", "France", user, 20.0);
    }

    private String generateUserId(TrainTicket ticket) {
        return ticket.getUser().getFirstName().toLowerCase() + "_" + ticket.getUser().getLastName().toLowerCase();
    }

    private void allocateSeat(String userId) {
        // Simple logic to alternate between sections A and B
        String section = (seatCounter++ % 2 == 0) ? "A" : "B";
        seatMap.put(userId, section);
    }

    static class TrainTicketRequest {
        private String firstName;
        private String lastName;
        private String email;

        // Getters and setters
    }

    static class User {
        private String firstName;
        private String lastName;
        private String email;

        public User(String firstName, String lastName, String email) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
        }

        // Getters and setters
    }

    static class TrainTicket {
        private String from;
        private String to;
        private User user;
        private double pricePaid;

        public TrainTicket(String from, String to, User user, double pricePaid) {
            this.from = from;
            this.to = to;
            this.user = user;
            this.pricePaid = pricePaid;
        }

        // Getters and setters

        @Override
        public String toString() {
            return "TrainTicket{" +
                    "from='" + from + '\'' +
                    ", to='" + to + '\'' +
                    ", user=" + user +
                    ", pricePaid=" + pricePaid +
                    ", seatSection='" + seatMap.get(generateUserId(this)) + '\'' +
                    '}';
        }
    }
}
