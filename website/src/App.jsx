import React, { useState } from 'react';
import { Routes, Route, useLocation } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Sidebar from './components/Sidebar';
import { Menu } from 'lucide-react';
import Dashboard from './pages/Dashboard';
import Donors from './pages/Donors';
import BloodStock from './pages/BloodStock';
import Requests from './pages/Requests';
import Login from './pages/Login';
import './App.css';

const PAGE_META = {
  '/': { title: 'Dashboard Overview', subtitle: 'Welcome back, Admin', container: "dashboard-container" },
  '/donors': { title: 'Donor Management', subtitle: 'Manage and track all blood donors', container: "donors-container" },
  '/stock': { title: 'Blood Stock', subtitle: 'Monitor blood inventory levels', container: "stock-container" },
  '/requests': { title: 'Donation Requests', subtitle: 'Manage and fulfill blood requests', container: "requests-container" },
  '/settings': { title: 'Settings', subtitle: 'Configure your admin preferences', container: "settings-container" },
};

function AdminLayout({ children }) {
  const [isSidebarOpen, setIsSidebarOpen] = useState(true);
  const location = useLocation();
  const meta = PAGE_META[location.pathname] || { title: 'BD-Admin', subtitle: '', container: "" };

  // Auto-close sidebar on mobile when navigating
  React.useEffect(() => {
    if (window.innerWidth <= 1024) {
      setIsSidebarOpen(false);
    }
  }, [location.pathname]);

  return (
    <div className={`layout-root ${!isSidebarOpen ? 'sidebar-closed' : ''}`}>
      {isSidebarOpen && (
        <div
          className="sidebar-backdrop"
          onClick={() => setIsSidebarOpen(false)}
        ></div>
      )}
      <Sidebar isOpen={isSidebarOpen} setIsOpen={setIsSidebarOpen} />
      <main className="main-content">
        <div className="top-nav-bar">
          <button
            className="sidebar-toggle-btn"
            onClick={() => setIsSidebarOpen(!isSidebarOpen)}
            aria-label="Toggle Sidebar"
          >
            <Menu size={24} />
          </button>
          <div className="top-nav-page-info">
            <h1 className="top-nav-title">{meta.title}</h1>
            {meta.subtitle && <p className="top-nav-subtitle">{meta.subtitle}</p>}
          </div>
        </div>
        <div className={`page-content ${meta.container}`}>
          {children}
        </div>
      </main>
    </div>
  );
}

function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/" element={
          <ProtectedRoute>
            <AdminLayout><Dashboard /></AdminLayout>
          </ProtectedRoute>
        } />
        <Route path="/donors" element={
          <ProtectedRoute>
            <AdminLayout><Donors /></AdminLayout>
          </ProtectedRoute>
        } />
        <Route path="/stock" element={
          <ProtectedRoute>
            <AdminLayout><BloodStock /></AdminLayout>
          </ProtectedRoute>
        } />
        <Route path="/requests" element={
          <ProtectedRoute>
            <AdminLayout><Requests /></AdminLayout>
          </ProtectedRoute>
        } />
        <Route path="/settings" element={
          <ProtectedRoute>
            <AdminLayout>
              <div className="p-8"><h1>Settings Coming Soon</h1></div>
            </AdminLayout>
          </ProtectedRoute>
        } />
      </Routes>
    </AuthProvider>
  );
}

export default App;
