import { useState } from 'react';

function VerticalNavBar() {
  const [expanded, setExpanded] = useState(false);

  const navItems = [
    { icon: '📄', label: 'Docs' },
    { icon: '📁', label: 'Files' },
    { icon: '💬', label: 'Messages' },
    { icon: '⚙️', label: 'Settings' },
    { icon: '🔍', label: 'Search' },
    { icon: '❓', label: 'Help' }
  ];

  return (
    <div
      style={{
        position: 'fixed',
        bottom: '20px',
        right: '20px',
        zIndex: 9999,
        display: 'flex',
        flexDirection: 'column-reverse',
        alignItems: 'center'
      }}
    >
      {/* Circular toggle button */}
      <div
        onClick={() => setExpanded(!expanded)}
        style={{
          width: '60px',
          height: '60px',
          borderRadius: '50%',
          backgroundColor: '#007bff',
          color: '#fff',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontSize: '24px',
          boxShadow: '0 4px 8px rgba(0,0,0,0.3)',
          cursor: 'pointer',
          marginBottom: expanded ? '0' : '0'
        }}
      >
        🔔
      </div>

      {/* Menu items */}
      {
        navItems.map((item, index) =>{

           const delay = `${index * 15}ms`;
           return  (
              <div
                key={index}
                title={item.label}
                style={{
                  width: '50px',
                  height: '50px',
                  marginBottom: '10px',
                  borderRadius: '12px',
                  backgroundColor: '#343a40',
                  color: 'white',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: '20px',
                  cursor: 'pointer',
                  transition: 'background 0.3s',
                  opacity: expanded ? 1 : 0,
                  transform: expanded ? 'translateY(0)' : 'translateY(60px)',
                  transition: `opacity 300ms ease-out ${delay}, transform 300ms ease-out ${delay}`,
                  pointerEvents: expanded ? 'auto' : 'none'
                }}
                onClick={() => alert(`Clicked ${item.label}`)}
              >
                {item.icon}
              </div>
           )
        }
        )}


    </div>
  );
};

export default VerticalNavBar;

