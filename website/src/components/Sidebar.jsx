import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
    LayoutDashboard,
    Users,
    Droplets,
    ClipboardList,
    Settings,
    LogOut,
    X
} from 'lucide-react';
import './Sidebar.css';

const Sidebar = ({ isOpen, setIsOpen }) => {
    const { logout } = useAuth();
    const navigate = useNavigate();

    const menuItems = [
        { icon: LayoutDashboard, label: 'Dashboard', path: '/' },
        { icon: Users, label: 'Donors', path: '/donors' },
        { icon: Droplets, label: 'Blood Stock', path: '/stock' },
        { icon: ClipboardList, label: 'Requests', path: '/requests' },
        { icon: Settings, label: 'Settings', path: '/settings' },
    ];

    async function handleLogout() {
        try {
            await logout();
            navigate('/login');
        } catch (err) {
            console.error('Logout failed:', err);
        }
    }

    return (
        <aside className={`sidebar glass ${!isOpen ? 'closed' : ''}`}>
            <div className="sidebar-header">
                <div className="logo-container">
                    <div className="logo-icon">
                        <img src="/icon.svg" alt="Drop" width={28} height={28} />
                    </div>
                    <span className="logo-text">BD-Admin</span>
                </div>
                <button
                    className="mobile-close-btn"
                    onClick={() => setIsOpen(false)}
                    aria-label="Close Sidebar"
                >
                    <X size={20} />
                </button>
            </div>

            <nav className="sidebar-nav">
                {menuItems.map((item) => (
                    <NavLink
                        key={item.path}
                        to={item.path}
                        className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
                    >
                        <item.icon size={20} />
                        <span>{item.label}</span>
                    </NavLink>
                ))}
            </nav>

            <div className="sidebar-footer">
                <button className="logout-btn" onClick={handleLogout}>
                    <LogOut size={20} />
                    <span>Logout</span>
                </button>
            </div>
        </aside>
    );
};

export default Sidebar;
