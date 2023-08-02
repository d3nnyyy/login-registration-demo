import './App.css';
import SignIn from './pages/SignIn';
import SignUp from './pages/SignUp'
import { BrowserRouter, Routes, Route } from "react-router-dom";

function App() {
  return (
    <>
    <BrowserRouter>
      <Routes>
          <Route path='/' element={<SignIn/>}/>
          <Route path='/signup' element={<SignUp/>}/>
      </Routes>
    </BrowserRouter>
      
    
     </>
  );
}

export default App;
