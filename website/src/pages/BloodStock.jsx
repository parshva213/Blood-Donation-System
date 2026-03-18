import React, { useState, useEffect } from 'react';
import { ref, onValue } from 'firebase/database';
import { rtdb } from '../firebase';
import {
    Droplets
} from 'lucide-react';
import './BloodStock.css';

const DEFAULT_GROUPS = ['O+', 'O-', 'A+', 'A-', 'B+', 'B-', 'AB+', 'AB-'];

const BloodStock = () => {
    const [stockData, setStockData] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const usersRef = ref(rtdb, 'Users');
        const historyRef = ref(rtdb, 'Donation_History');

        let usersData = {};
        let historyData = {};

        const calculateStock = () => {
            const stockMap = {};
            DEFAULT_GROUPS.forEach(g => stockMap[g] = 0);

            if (historyData) {
                Object.values(historyData).forEach(record => {
                    // Only sum verified donations that haven't been deleted
                    if (record.status === 'Verified' && record.deleted === 0 && record.donorId) {
                        const user = usersData[record.donorId];
                        if (user && user.bloodGroup) {
                            const bg = user.bloodGroup;
                            if (stockMap[bg] !== undefined) {
                                stockMap[bg] += (Number(record.units) || 1);
                            }
                        }
                    }
                });
            }

            const stockList = DEFAULT_GROUPS.map(group => {
                const units = stockMap[group];
                return {
                    id: group,
                    group: group,
                    units: units,
                    status: units >= 30 ? 'High' : units >= 15 ? 'Good' : units >= 5 ? 'Low' : 'Critical',
                    color: units < 10 ? '#3b82f6' : '#ef4444',
                };
            });

            setStockData(stockList);
            setLoading(false);
        };

        const unsubUsers = onValue(usersRef, (snapshot) => {
            usersData = snapshot.val() || {};
            calculateStock();
        }, (error) => {
            console.error('Error fetching users for stock:', error);
            setLoading(false);
        });

        const unsubHistory = onValue(historyRef, (snapshot) => {
            historyData = snapshot.val() || {};
            calculateStock();
        }, (error) => {
            console.error('Error fetching history for stock:', error);
            setLoading(false);
        });

        return () => {
            unsubUsers();
            unsubHistory();
        };
    }, []);

    return (
        <>
            {loading ? (
                <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                    Loading blood stock from Firebase...
                </div>
            ) : (
                <div className="stock-grid">
                    {stockData.map((item) => (
                        <div key={item.id} className="stock-card premium-card">
                            <div className="stock-card-header">
                                <span className="group-label">{item.group}</span>
                                <Droplets size={24} color={item.color} fill={item.color} />
                            </div>
                            <div className="stock-value">
                                <h2>{item.units}</h2>
                                <p>Units</p>
                            </div>
                            <div className={`stock-status ${item.status.toLowerCase()}`}>
                                {item.status}
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </>
    );
};

export default BloodStock;
