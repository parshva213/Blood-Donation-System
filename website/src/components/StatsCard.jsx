import React from 'react';
import { motion } from 'framer-motion';
import './StatsCard.css';

const StatsCard = ({ title, value, icon: Icon, color, trend }) => {
    return (
        <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className="stats-card premium-card"
        >
            <div className="stats-header">
                <div className="stats-icon" style={{ backgroundColor: `${color}15`, color: color }}>
                    <Icon size={24} />
                </div>
                {trend && (
                    <span className={`stats-trend ${trend > 0 ? 'trend-up' : 'trend-down'}`}>
                        {trend > 0 ? '+' : ''}{trend}%
                    </span>
                )}
            </div>
            <div className="stats-info">
                <h3>{value}</h3>
                <p>{title}</p>
            </div>
        </motion.div>
    );
};

export default StatsCard;
