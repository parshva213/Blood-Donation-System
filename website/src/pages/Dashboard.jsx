import React, { useState, useEffect } from 'react';
import { ref, onValue } from 'firebase/database';
import { rtdb } from '../firebase';
import {
    Users,
    Droplets,
    Activity
} from 'lucide-react';
import StatsCard from '../components/StatsCard';
import {
    BarChart,
    Bar,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    ResponsiveContainer,
    AreaChart,
    Area
} from 'recharts';
import './Dashboard.css';



const Dashboard = () => {
    const [stats, setStats] = useState({
        totalDonors: 0,
        totalDonations: 0,
        pendingRequests: 0,
    });
    const [chartData, setChartData] = useState([]);
    const [barChartData, setBarChartData] = useState([]);

    const historyDataRef = React.useRef(null);
    const requestsDataRef = React.useRef(null);
    const usersDataRef = React.useRef(null);

    function rebuildChartData() {
        const historyData = historyDataRef.current;
        const requestsData = requestsDataRef.current;

        const days = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
        const today = new Date();
        const last7Days = [];

        // Initialize array for the last 7 days
        for (let i = 6; i >= 0; i--) {
            const d = new Date(today);
            d.setDate(d.getDate() - i);
            // Get local YYYY-MM-DD
            const year = d.getFullYear();
            const month = String(d.getMonth() + 1).padStart(2, '0');
            const day = String(d.getDate()).padStart(2, '0');
            const dateStr = `${year}-${month}-${day}`;

            last7Days.push({
                dateStr,
                name: `${day}/${month}`,
                donations: 0,
                requests: 0
            });
        }

        const bgData = {
            'O+': { name: 'O+', donations: 0, requests: 0 },
            'O-': { name: 'O-', donations: 0, requests: 0 },
            'A+': { name: 'A+', donations: 0, requests: 0 },
            'A-': { name: 'A-', donations: 0, requests: 0 },
            'B+': { name: 'B+', donations: 0, requests: 0 },
            'B-': { name: 'B-', donations: 0, requests: 0 },
            'AB+': { name: 'AB+', donations: 0, requests: 0 },
            'AB-': { name: 'AB-', donations: 0, requests: 0 },
        };

        // Aggregate Donations
        if (historyData) {
            Object.values(historyData).forEach(record => {
                if (record.status === 'Verified' && record.deleted === 0 && record.date) {
                    const rDate = record.date.split(' ')[0];
                    const dayItem = last7Days.find(d => d.dateStr === rDate);
                    if (dayItem) {
                        dayItem.donations += Number(record.units || 1);
                    }
                    if (usersDataRef.current && record.donorId) {
                        const user = usersDataRef.current[record.donorId];
                        if (user && user.bloodGroup && bgData[user.bloodGroup]) {
                            bgData[user.bloodGroup].donations += Number(record.units || 1);
                        }
                    }
                }
            });
        }

        // Aggregate Requests
        if (requestsData) {
            Object.values(requestsData).forEach(record => {
                if (record.date) {
                    const rDate = record.date.split(' ')[0];
                    const dayItem = last7Days.find(d => d.dateStr === rDate);
                    if (dayItem) {
                        dayItem.requests += Number(record.units || 1);
                    }
                }
                if (record.bloodGroup && bgData[record.bloodGroup]) {
                    bgData[record.bloodGroup].requests += Number(record.units || 1);
                }
            });
        }

        setChartData(last7Days);
        setBarChartData(Object.values(bgData));
    }

    useEffect(() => {
        // Listen to donors count
        const donorsRef = ref(rtdb, 'Users');
        const unsubDonors = onValue(donorsRef, (snapshot) => {
            const data = snapshot.val();
            let count = 0;
            if (data) {
                usersDataRef.current = data;
                // Count only non-admin users
                count = Object.values(data).filter(user => user.role !== 'admin').length;
            }
            setStats(prev => ({ ...prev, totalDonors: count }));
            rebuildChartData();
        }, (error) => {
            console.error('Error fetching donors:', error);
        });

        // Listen to total donations history
        const historyRef = ref(rtdb, 'Donation_History');
        const unsubHistory = onValue(historyRef, (snapshot) => {
            const data = snapshot.val();
            historyDataRef.current = data;
            if (data) {
                // Sum up units from all verified donations
                const totalUnits = Object.values(data)
                    .filter(record => record.status === 'Verified' && record.deleted === 0)
                    .reduce((sum, item) => sum + (Number(item.units) || 1), 0);
                setStats(prev => ({ ...prev, totalDonations: totalUnits }));
            }
            rebuildChartData();
        }, (error) => {
            console.error('Error fetching history:', error);
        });

        // Listen to requests
        const requestsRef = ref(rtdb, 'Requests');
        const unsubRequests = onValue(requestsRef, (snapshot) => {
            const data = snapshot.val();
            requestsDataRef.current = data;
            if (data) {
                const pending = Object.values(data).filter(r => r.status === 'Pending').length;
                setStats(prev => ({ ...prev, pendingRequests: pending }));
            }
            rebuildChartData();
        }, (error) => {
            console.error('Error fetching requests:', error);
        });

        return () => {
            unsubDonors();
            unsubHistory();
            unsubRequests();
        };
    }, []);

    return (
        <div className="grid">
            <div className="stats-grid">
                <StatsCard
                    title="Total Donors"
                    value={stats.totalDonors.toLocaleString()}
                    icon={Users}
                    color="#3b82f6"
                />
                <StatsCard
                    title="Total Donations (Units)"
                    value={stats.totalDonations.toLocaleString()}
                    icon={Droplets}
                    color="#ef4444"
                />
                <StatsCard
                    title="Pending Requests"
                    value={stats.pendingRequests.toString()}
                    icon={Activity}
                    color="#f59e0b"
                />
            </div>

            <div className="charts-grid">
                <div className="chart-container premium-card">
                    <h3>Donation Trends</h3>
                    <div className="chart-wrapper">
                        <ResponsiveContainer width="100%" height={300}>
                            <AreaChart data={chartData}>
                                <defs>
                                    <linearGradient id="colorDonations" x1="0" y1="0" x2="0" y2="1">
                                        <stop offset="5%" stopColor="#ef4444" stopOpacity={0.3} />
                                        <stop offset="95%" stopColor="#ef4444" stopOpacity={0} />
                                    </linearGradient>
                                </defs>
                                <CartesianGrid strokeDasharray="3 3" stroke="#334155" vertical={false} />
                                <XAxis dataKey="name" stroke="#94a3b8" />
                                <YAxis stroke="#94a3b8" />
                                <Tooltip
                                    contentStyle={{ backgroundColor: '#1e293b', border: '1px solid #334155', borderRadius: '8px' }}
                                />
                                <Area type="monotone" dataKey="donations" stroke="#ef4444" fillOpacity={1} fill="url(#colorDonations)" />
                            </AreaChart>
                        </ResponsiveContainer>
                    </div>
                </div>

                <div className="chart-container premium-card">
                    <h3>Blood Group: Donations vs Requests</h3>
                    <div className="chart-wrapper">
                        <ResponsiveContainer width="100%" height={300}>
                            <BarChart data={barChartData}>
                                <CartesianGrid strokeDasharray="3 3" stroke="#334155" vertical={false} />
                                <XAxis dataKey="name" stroke="#94a3b8" />
                                <YAxis stroke="#94a3b8" />
                                <Tooltip
                                    contentStyle={{ backgroundColor: '#1e293b', border: '1px solid #334155', borderRadius: '8px' }}
                                />
                                <Bar dataKey="donations" name="Donations" fill="#10b981" radius={[4, 4, 0, 0]} />
                                <Bar dataKey="requests" name="Requests" fill="#ef4444" radius={[4, 4, 0, 0]} />
                            </BarChart>
                        </ResponsiveContainer>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Dashboard;
