import './App.css';
import SignIn from './pages/SignIn';
import SignUp from './pages/SignUp'
import Home from './pages/Home'
import ForgotPassword from './pages/ForgotPassword';
import Verified from './pages/Verified';
import { BrowserRouter, Routes, Route } from "react-router-dom";

function App() {
  return (
    <>
    <BrowserRouter>
      <Routes>
          <Route path='/verified' element={<Verified/>}/>
          <Route path='/' element={<Home/>}/>
          <Route path='/forgotpassword' element={<ForgotPassword/>}/>
          <Route path='/login' element={<SignIn/>}/>
          <Route path='/signup' element={<SignUp/>}/>
      </Routes>
    </BrowserRouter>
      
    
     </>
  );
}

export default App;
