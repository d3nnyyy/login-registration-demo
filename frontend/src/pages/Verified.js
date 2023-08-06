import Typography from "@mui/material/Typography";
import { Avatar } from "@mui/material";
import DoneIcon from "@mui/icons-material/Done";
import Container from "@mui/material/Container";
import { createTheme } from "@mui/material";
import { ThemeProvider } from "@mui/material";
import { green } from "@mui/material/colors";
import { Link as RouterLink } from "react-router-dom";
import Link from "@mui/material/Link";
export default function Verified() {
  const defaultTheme = createTheme();
  return (
    <ThemeProvider theme={defaultTheme}>
      <Container
        sx={{
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          justifyContent: "flex-start",
          height: "100vh",
        }}
      >
        <Avatar sx={{ mt: 2, bgcolor: green[500] }}>
          <DoneIcon />
        </Avatar>
        <Typography component="h1" variant="h5" sx={{ mt: 1 }}>
          Your email has been verified!
        </Typography>
        <Link sx={{ mt: 1 }} component={RouterLink} to="/" variant="body2">
          <Typography component="h1" variant="h6">
            Back to home page
          </Typography>
        </Link>
      </Container>
    </ThemeProvider>
  );
}
