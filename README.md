# BeePOS - Point of Sale System

BeePOS is a modern, JavaFX-based Point of Sale system designed for small to medium-sized businesses. It features a clean, dark-themed UI and supports role-based access control for sellers and managers.

## Features

### For Sellers
- Product browsing and filtering by category
- Easy bill creation and management
- Real-time total calculation
- Cash payment handling with change calculation
- Sales statistics viewing
- Stock level monitoring

### For Managers
- Complete inventory management
  - Add new products
  - Update existing products
  - Delete products
  - Monitor stock levels
- Sales statistics and reporting
  - Total sales overview
  - Top-selling products
  - Low stock alerts
  - Detailed sales history

## Prerequisites

- Java Development Kit (JDK) 17 or later
- Maven 3.6 or later
- SQLite 3

## Installation

1. Clone the repository:
```bash
git clone https://github.com/yourusername/BeePOS.git
cd BeePOS
```

2. Build the project:
```bash
mvn clean install
```

3. Run the application:
```bash
mvn javafx:run
```

## Default Login Credentials

### Seller Account
- Username: seller1
- Password: password123

### Manager Account
- Username: manager1
- Password: admin123

## Project Structure

```
BeePOS/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── beepos/
│       │           ├── controllers/
│       │           ├── models/
│       │           └── utils/
│       └── resources/
│           ├── css/
│           ├── fxml/
│           └── images/
├── .env
├── .gitignore
└── pom.xml
```

## Database

The application uses SQLite for data persistence. The database file (`beepos.db`) is automatically created on first run and includes:
- Users table (authentication)
- Products table (inventory)
- Sales table (transactions)

## Styling

The application uses a modern dark theme with:
- Dark backgrounds (#1E1E1E)
- Yellow accents (#FFD700)
- Clean, minimalist UI elements
- Responsive layout

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- JavaFX for the UI framework
- SQLite for the embedded database
- Maven for project management
