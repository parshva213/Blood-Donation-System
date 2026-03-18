import React, { useState, useEffect } from 'react';
import { ref, onValue } from 'firebase/database';
import { rtdb } from '../firebase';
import {
    Search,
    Phone,
    Droplet
} from 'lucide-react';
import './Donors.css';

const Donors = () => {
    const [donors, setDonors] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [loading, setLoading] = useState(true);

    // Store raw data from both listeners
    const donorsDataRef = React.useRef(null);
    const historyDataRef = React.useRef(null);

    function rebuildDonorList() {
        const donorsData = donorsDataRef.current;
        const historyData = historyDataRef.current;

        const lastDonationMap = {};
        const unitsMap = {};
        if (historyData) {
            Object.values(historyData).forEach(record => {
                const uid = record.donorId || record.userId || record.uid;
                const date = record.date || record.donationDate;
                const units = record.units || record.quantity || 1;
                if (uid) {
                    unitsMap[uid] = (unitsMap[uid] || 0) + Number(units);
                    if (date && (!lastDonationMap[uid] || date > lastDonationMap[uid])) {
                        lastDonationMap[uid] = date;
                    }
                }
            });
        }

        if (donorsData) {
            const donorList = Object.entries(donorsData)
                .filter(([key, val]) => val.role !== 'admin')
                .map(([key, val]) => ({
                    id: key,
                    ...val,
                    lastDonated: lastDonationMap[val.uid || key] || null,
                    unitsDonated: unitsMap[val.uid || key] || 0,
                }));
            setDonors(donorList);
        } else {
            setDonors([]);
        }
        setLoading(false);
    }

    useEffect(() => {
        const donorsRef = ref(rtdb, 'Users');
        const historyRef = ref(rtdb, 'Donation_History');

        const unsubDonors = onValue(donorsRef, (snapshot) => {
            donorsDataRef.current = snapshot.val();
            rebuildDonorList();
        });

        const unsubHistory = onValue(historyRef, (snapshot) => {
            historyDataRef.current = snapshot.val();
            rebuildDonorList();
        });

        return () => {
            unsubDonors();
            unsubHistory();
        };
    }, []);

    const filteredDonors = donors.filter(donor => {
        const term = searchTerm.toLowerCase();
        return (
            (donor.fullName || '').toLowerCase().includes(term) ||
            (donor.email || '').toLowerCase().includes(term) ||
            (donor.bloodGroup || '').toLowerCase().includes(term)
        );
    });

    return (
        <>

            <div className="table-controls glass">
                <div className="search-bar">
                    <Search size={20} color="#94a3b8" />
                    <input
                        type="text"
                        placeholder="Search by name, email or blood group..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>
            </div>

            <div className="table-wrapper premium-card">
                {loading ? (
                    <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                        Loading donors from Firebase...
                    </div>
                ) : filteredDonors.length === 0 ? (
                    <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                        {searchTerm ? 'No donors match your search.' : 'No donors found in database.'}
                    </div>
                ) : (
                    <table className="donors-table">
                        <thead>
                            <tr>
                                <th>Donor Name</th>
                                <th>Blood Group</th>
                                <th>Units Donated</th>
                                <th>Last Donation</th>
                                <th>Contact Info</th>
                            </tr>
                        </thead>
                        <tbody>
                            {filteredDonors.map((donor) => (
                                <tr key={donor.id}>
                                    <td>
                                        <div className="donor-name-cell">
                                            <div className="avatar">{(donor.fullName || '?').charAt(0)}</div>
                                            <div>
                                                <p className="name-text">{donor.fullName || 'Unknown'}</p>
                                                <p className="email-text">{donor.email || 'No email'}</p>
                                            </div>
                                        </div>
                                    </td>
                                    <td>
                                        <span className={`blood-badge ${(donor.bloodGroup || '').replace('+', 'plus').replace('-', 'minus')}`}>
                                            <Droplet size={14} fill="currentColor" />
                                            {donor.bloodGroup || 'N/A'}
                                        </span>
                                    </td>
                                    <td><span style={{ fontWeight: 600 }}>{donor.unitsDonated}</span></td>
                                    <td>{donor.lastDonated || 'N/A'}</td>
                                    <td>
                                        <div className="contact-cell">
                                            <Phone size={14} /> {donor.contact || donor.phone || 'N/A'}
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </div>
        </>
    );
};

export default Donors;
