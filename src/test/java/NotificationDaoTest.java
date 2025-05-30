import model.dao.impl.BaseDao;
import model.dao.impl.NotificationDao;
import model.dao.impl.ReservationDao;
import model.dao.impl.UserDao;
import model.entity.Book;
import model.entity.Notification;
import model.entity.Reservation;
import model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.sql.*;
import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

public class NotificationDaoTest {

    private NotificationDao notificationDao;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    public void setUp() throws SQLException {
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
        notificationDao = new NotificationDao();
        BaseDao.setConnection(mockConnection);
//        notificationDao.setConnection(mockConnection);
    }


    @Test
    public void testSaveNotification() throws SQLException {
        Notification notification = new Notification();
        Reservation reservation = new Reservation();
        reservation.setReservationId(1L);

        // Set the Reservation object in the Notification
        notification.setReservation(reservation);

        User user = new User();
        user.setUserId(1L); // Set the userId
        notification.setUser(user);

        notification.setMessageEnglish("Test message in English");
        notification.setMessageUrdu("ٹیسٹ پیغام اردو میں");
        notification.setMessageRussian("Тестовое сообщение на русском");
        notification.setCreatedAt(LocalDateTime.now());

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        notificationDao.saveNotification(notification);

        verify(mockConnection).prepareStatement(anyString());
        verify(mockPreparedStatement).setLong(1, notification.getUser().getUserId());
        verify(mockPreparedStatement).setString(2, notification.getMessageEnglish());
        verify(mockPreparedStatement).setString(3, notification.getMessageUrdu());
        verify(mockPreparedStatement).setString(4, notification.getMessageRussian());
        verify(mockPreparedStatement).setTimestamp(5, java.sql.Timestamp.valueOf(notification.getCreatedAt()));
        verify(mockPreparedStatement).setLong(6, notification.getReservationId());
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    public void testGetNotificationsByUserId() throws SQLException, NoSuchFieldException, IllegalAccessException {

            // Mock userDao and its methods
            UserDao mockUserDao = mock(UserDao.class);
            User user = new User();
            user.setUserId(1L);
            when(mockUserDao.getUserById(1L)).thenReturn(user);

            // Use reflection to set the userDao field in NotificationDao
            Field userDaoField = NotificationDao.class.getDeclaredField("userDao");
            userDaoField.setAccessible(true);
            userDaoField.set(notificationDao, mockUserDao);

            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true).thenReturn(false);
            when(mockResultSet.getLong("notification_id")).thenReturn(1L);
            when(mockResultSet.getString("message_en")).thenReturn("Test message in English");
            when(mockResultSet.getString("message_ur")).thenReturn("ٹیسٹ پیغام اردو میں");
            when(mockResultSet.getString("message_ru")).thenReturn("Тестовое сообщение на русском");
            when(mockResultSet.getTimestamp("created_at")).thenReturn(java.sql.Timestamp.valueOf(LocalDateTime.now()));
            when(mockResultSet.getLong("user_id")).thenReturn(1L);

            notificationDao.getNotificationsByUserId(1L);

            verify(mockConnection).prepareStatement(anyString());
            verify(mockPreparedStatement).setLong(1, 1L);
            verify(mockPreparedStatement).executeQuery();
            verify(mockResultSet, Mockito.times(2)).next();
            verify(mockResultSet).getLong("notification_id");
            verify(mockResultSet).getString("message_en");
            verify(mockResultSet).getString("message_ur");
            verify(mockResultSet).getString("message_ru");
            verify(mockResultSet).getTimestamp("created_at");
            verify(mockResultSet).getLong("user_id");


    }

    @Test
    public void testCreateNotificationForReservation() throws IllegalAccessException, NoSuchFieldException, SQLException {
        ReservationDao mockReservationDao = mock(ReservationDao.class);
        Reservation reservation = new Reservation();
        reservation.setReservationId(1L);
        User user = new User();
        user.setUsername("username");
        user.setUserId(1L);
        reservation.setUser(user);
        Book book = new Book();
        book.setTitleEn("title in English");
        book.setTitleUr("title in Urdu");
        book.setTitleRu("title in Russian");
        reservation.setBook(book);
        reservation.setDueDate(LocalDateTime.now().plusDays(7));
        when(mockReservationDao.getReservationById(1L)).thenReturn(reservation);

        // Use reflection to set the reservationDao field in NotificationDao
        Field reservationDaoField = NotificationDao.class.getDeclaredField("reservationDao");
        reservationDaoField.setAccessible(true);
        reservationDaoField.set(notificationDao, mockReservationDao);

        // Mock the PreparedStatement
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        notificationDao.createNotificationForReservation(1L);

        verify(mockReservationDao).getReservationById(1L);
        verify(mockConnection).prepareStatement(anyString());
        verify(mockPreparedStatement).setLong(eq(1), eq(user.getUserId()));
        verify(mockPreparedStatement).setString(eq(2), eq("Dear username, you have borrowed the book 'title in English'. Please return it by " + reservation.getDueDate() + "."));
        verify(mockPreparedStatement).setString(eq(3), eq("محترم username, آپ نے کتاب 'title in Urdu' ادھار لی ہے۔ براہ کرم اسے " + reservation.getDueDate() + " تک واپس کریں۔"));
        verify(mockPreparedStatement).setString(eq(4), eq("Уважаемый username, вы взяли книгу 'title in Russian'. Пожалуйста, верните её до " + reservation.getDueDate() + "."));
        verify(mockPreparedStatement).setTimestamp(eq(5), any(Timestamp.class));
        verify(mockPreparedStatement).setLong(eq(6), eq(reservation.getReservationId()));
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    public void testDeleteNotificationByReservationId() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        notificationDao.deleteNotification(1L);
        verify(mockConnection).prepareStatement(anyString());
        verify(mockPreparedStatement).setLong(1, 1L);
        verify(mockPreparedStatement).executeUpdate();
    }


    @Test
    public void testUpdateNotification() throws SQLException, NoSuchFieldException, IllegalAccessException {
        ReservationDao mockReservationDao = mock(ReservationDao.class);
        Reservation reservation = new Reservation();
        reservation.setReservationId(1L);
        User user = new User();
        user.setUsername("username");
        user.setUserId(1L);
        reservation.setUser(user);
        Book book = new Book();
        book.setTitleEn("title in English");
        book.setTitleUr("title in Urdu");
        book.setTitleRu("title in Russian");
        reservation.setBook(book);
        reservation.setDueDate(LocalDateTime.now().plusDays(7));
        reservation.setBorrowDate(LocalDateTime.now().minusDays(3));
        when(mockReservationDao.getReservationById(1L)).thenReturn(reservation);

        // Use reflection to set the reservationDao field in NotificationDao
        Field reservationDaoField = NotificationDao.class.getDeclaredField("reservationDao");
        reservationDaoField.setAccessible(true);
        reservationDaoField.set(notificationDao, mockReservationDao);

        // Mock the ResultSet for getReservationById
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true).thenReturn(false);
        when(mockResultSet.getLong("reservation_id")).thenReturn(1L);
        when(mockResultSet.getLong("user_id")).thenReturn(1L);
        when(mockResultSet.getString("username")).thenReturn("username");
        when(mockResultSet.getString("title_en")).thenReturn("title in English");
        when(mockResultSet.getString("title_ur")).thenReturn("title in Urdu");
        when(mockResultSet.getString("title_ru")).thenReturn("title in Russian");
        when(mockResultSet.getTimestamp("due_date")).thenReturn(Timestamp.valueOf(reservation.getDueDate()));
        when(mockResultSet.getTimestamp("borrow_date")).thenReturn(Timestamp.valueOf(reservation.getBorrowDate()));

        notificationDao.updateNotification(1L);

        verify(mockConnection, times(2)).prepareStatement(anyString());
        verify(mockPreparedStatement).setString(eq(1), eq("Reservation extended. Dear username, you have borrowed the book 'title in English'. Please return it by " + reservation.getDueDate() + "."));
        verify(mockPreparedStatement).setString(eq(2), eq("ریزرویشن میں توسیع کی گئی۔ محترم username, آپ نے کتاب 'title in Urdu' ادھار لی ہے۔ براہ کرم اسے " + reservation.getDueDate() + " تک واپس کریں۔"));
        verify(mockPreparedStatement).setString(eq(3), eq("Продление бронирования. Уважаемый username, вы взяли книгу 'title in Russian'. Пожалуйста, верните её до " + reservation.getDueDate() + "."));
        verify(mockPreparedStatement).setLong(eq(4), eq(1L));
        verify(mockPreparedStatement).executeUpdate();
    }


    @Test
    public void testDeleteNotificationByReservationIdSQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Test SQL Exception"));

        notificationDao.deleteNotification(1L);

        verify(mockConnection).prepareStatement(anyString());
        verify(mockPreparedStatement).executeUpdate();
    }


    @Test
    public void testUpdateNotificationByReservationIdSQLException() throws SQLException, NoSuchFieldException, IllegalAccessException {
        // Mock ReservationDao and its methods
        ReservationDao mockReservationDao = mock(ReservationDao.class);
        Reservation reservation = new Reservation();
        reservation.setReservationId(1L);
        User user = new User();
        user.setUserId(1L);
        user.setUsername("username");
        reservation.setUser(user);
        Book book = new Book();
        book.setTitleEn("title in English");
        book.setTitleUr("title in Urdu");
        book.setTitleRu("title in Russian");
        reservation.setBook(book);
        reservation.setDueDate(LocalDateTime.now().plusDays(7));
        reservation.setBorrowDate(LocalDateTime.now().minusDays(3));
        when(mockReservationDao.getReservationById(1L)).thenReturn(reservation);

        // Use reflection to set the reservationDao field in NotificationDao
        Field reservationDaoField = NotificationDao.class.getDeclaredField("reservationDao");
        reservationDaoField.setAccessible(true);
        reservationDaoField.set(notificationDao, mockReservationDao);

        // Mock the PreparedStatement and ResultSet
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true).thenReturn(false);
        when(mockResultSet.getLong("reservation_id")).thenReturn(1L);
        when(mockResultSet.getLong("user_id")).thenReturn(1L);
        when(mockResultSet.getString("username")).thenReturn("username");
        when(mockResultSet.getString("title_en")).thenReturn("title in English");
        when(mockResultSet.getString("title_ur")).thenReturn("title in Urdu");
        when(mockResultSet.getString("title_ru")).thenReturn("title in Russian");
        when(mockResultSet.getTimestamp("due_date")).thenReturn(Timestamp.valueOf(reservation.getDueDate()));
        when(mockResultSet.getTimestamp("borrow_date")).thenReturn(Timestamp.valueOf(reservation.getBorrowDate()));

        // Mock the executeUpdate to throw SQLException
        when(mockPreparedStatement.executeUpdate()).thenThrow(new SQLException("Test SQL Exception"));

        // Call the method under test
        notificationDao.updateNotification(1L);

        // Verify interactions
        verify(mockConnection, times(2)).prepareStatement(anyString());
        verify(mockPreparedStatement).setString(eq(1), eq("Reservation extended. Dear username, you have borrowed the book 'title in English'. Please return it by " + reservation.getDueDate() + "."));
        verify(mockPreparedStatement).setString(eq(2), eq("ریزرویشن میں توسیع کی گئی۔ محترم username, آپ نے کتاب 'title in Urdu' ادھار لی ہے۔ براہ کرم اسے " + reservation.getDueDate() + " تک واپس کریں۔"));
        verify(mockPreparedStatement).setString(eq(3), eq("Продление бронирования. Уважаемый username, вы взяли книгу 'title in Russian'. Пожалуйста, верните её до " + reservation.getDueDate() + "."));
        verify(mockPreparedStatement).setLong(eq(4), eq(1L));
        verify(mockPreparedStatement).executeUpdate();
    }


    @Test
    public void testGetNotificationsByUserIdSQLException() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException("Test SQL Exception"));

        notificationDao.getNotificationsByUserId(1L);

        verify(mockConnection).prepareStatement(anyString());
        verify(mockPreparedStatement).executeQuery();
    }

}
