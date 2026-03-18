import React, { useState, useEffect } from 'react';
import { ref, onValue, update } from 'firebase/database';
import { rtdb } from '../firebase';
import {
    ClipboardList,
    CircleCheck,
    Clock
} from 'lucide-react';
import './Requests.css';

const Requests = () => {
    const [requests, setRequests] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const requestsRef = ref(rtdb, 'Requests');
        const unsub = onValue(requestsRef, (snapshot) => {
            const data = snapshot.val();
            if (data) {
                const requestList = Object.entries(data).map(([key, val]) => ({
                    id: key,
                    ...val,
                }));
                setRequests(requestList);
            } else {
                setRequests([]);
            }
            setLoading(false);
        }, (error) => {
            console.error('Error fetching requests:', error);
            setLoading(false);
        });

        return () => unsub();
    }, []);

    async function handleStatusUpdate(requestId, newStatus) {
        try {
            const requestRef = ref(rtdb, `Requests/${requestId}`);
            await update(requestRef, { status: newStatus });
        } catch (error) {
            console.error('Failed to update request:', error);
        }
    }

    return (
        <>

            {loading ? (
                <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                    Loading requests from Firebase...
                </div>
            ) : requests.length === 0 ? (
                <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                    No donation requests found in database.
                </div>
            ) : (
                <div className="requests-list">
                    {requests.map((request) => (
                        <div key={request.id} className="request-card premium-card">
                            <div className="request-card-left">
                                <div className={`priority-indicator ${(request.priority || 'normal').toLowerCase()}`}></div>
                                <div>
                                    <div className="request-id-row">
                                        <span className="request-id">{request.id}</span>
                                        <span className={`priority-badge ${(request.priority || 'normal').toLowerCase()}`}>
                                            {request.priority || 'Normal'}
                                        </span>
                                    </div>
                                    <h3 className="hospital-name">{request.patientName || request.hospital || 'Unknown Patient'}</h3>
                                    <p className="request-date">{request.hospital} • {request.date || 'No date'}</p>
                                    <p className="request-contact" style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginTop: '4px' }}>
                                        {request.contactNumber} {request.altContactNumber ? ` / ${request.altContactNumber}` : ''}
                                    </p>
                                </div>
                            </div>

                            <div className="request-card-center">
                                <div className="blood-info">
                                    <span className="blood-group-large">{request.bloodGroup || 'N/A'}</span>
                                    <span className="units-text">{request.units || 0} Units</span>
                                </div>
                            </div>

                            <div className="request-card-right">
                                <div className="status-container">
                                    <span className={`status-text ${(request.status || 'pending').toLowerCase()}`}>
                                        {request.status === 'Pending' && <Clock size={16} />}
                                        {request.status === 'Approved' && <CircleCheck size={16} />}
                                        {request.status === 'Completed' && <CircleCheck size={16} />}
                                        {request.status || 'Pending'}
                                    </span>
                                </div>
                                <div className="request-actions">
                                    {request.status === 'Pending' && (
                                        <>
                                            <button className="approve-btn" onClick={() => handleStatusUpdate(request.id, 'Approved')}>
                                                Approve
                                            </button>
                                            <button className="reject-btn" onClick={() => handleStatusUpdate(request.id, 'Rejected')}>
                                                Reject
                                            </button>
                                        </>
                                    )}
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </>
    );
};

export default Requests;
