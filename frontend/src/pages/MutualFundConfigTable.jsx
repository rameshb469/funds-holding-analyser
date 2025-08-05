import React, { useState } from "react";

const fundHouseOptions = ["SBI Mutual Fund", "Mirae Asset", "Axis MF", "HDFC MF"];
const fundNameOptions = ["Bluechip Fund", "Largecap Fund", "Flexicap Fund", "Midcap Fund"];
const fundTypeOptions = ["Equity", "Debt", "Hybrid", "Index"];

const initialData = [
  {
    house: "SBI Mutual Fund",
    name: "SBI Bluechip Fund",
    type: "Equity",
    configUrl: "https://example.com/sbi-bluechip",
    mapper1: "YYYY/MM",
    mapper2: "M+1",
    mapper3: "Active",
    mapper4: "Valid",
    success: 12,
    failed: 1,
  },
];

const MutualFundConfigTable = () => {
  const [data, setData] = useState(initialData);
  const [showAddModal, setShowAddModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [selectedIndex, setSelectedIndex] = useState(null);
  const [formData, setFormData] = useState({});

  const openAddModal = () => {
    setFormData({});
    setShowAddModal(true);
  };

  const openEditModal = (index) => {
    setSelectedIndex(index);
    setFormData(data[index]);
    setShowEditModal(true);
  };

  const openDeleteModal = (index) => {
    setSelectedIndex(index);
    setShowDeleteModal(true);
  };

  const closeModals = () => {
    setShowAddModal(false);
    setShowEditModal(false);
    setShowDeleteModal(false);
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleAdd = () => {
    if (!formData.house || !formData.name || !formData.type) return;
    const fullName = `${formData.house.split(" ")[0]} ${formData.name}`;
    setData((prev) => [...prev, { ...formData, name: fullName, success: 0, failed: 0 }]);
    closeModals();
  };

  const handleEdit = () => {
    const updated = [...data];
    updated[selectedIndex] = { ...updated[selectedIndex], ...formData };
    setData(updated);
    closeModals();
  };

  const handleDelete = () => {
    const updated = [...data];
    updated.splice(selectedIndex, 1);
    setData(updated);
    closeModals();
  };

  return (
    <div className="relative p-4">
      <div className="flex justify-end mb-2">
        <button
          onClick={openAddModal}
          className="px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700"
        >
          Add Configuration
        </button>
      </div>

      <table className="min-w-full table-auto border-collapse border border-gray-300">
        <thead>
          <tr className="bg-gray-100 text-left">
            <th className="p-2 border">Mutual Fund House</th>
            <th className="p-2 border">MF Name</th>
            <th className="p-2 border">Type of MF</th>
            <th className="p-2 border">Configuration URL</th>
            <th className="p-2 border">Data Mapper 1</th>
            <th className="p-2 border">Data Mapper 2</th>
            <th className="p-2 border">Data Mapper 3</th>
            <th className="p-2 border">Data Mapper 4</th>
            <th className="p-2 border">Success Count</th>
            <th className="p-2 border">Failed Count</th>
            <th className="p-2 border">Actions</th>
          </tr>
        </thead>
        <tbody>
          {data.map((fund, idx) => (
            <tr key={idx} className="hover:bg-gray-50">
              <td className="p-2 border">{fund.house}</td>
              <td className="p-2 border">{fund.name}</td>
              <td className="p-2 border">{fund.type}</td>
              <td className="p-2 border">
                <a href={fund.configUrl} className="text-blue-600" target="_blank" rel="noreferrer">
                  Link
                </a>
              </td>
              <td className="p-2 border">{fund.mapper1}</td>
              <td className="p-2 border">{fund.mapper2}</td>
              <td className="p-2 border">{fund.mapper3}</td>
              <td className="p-2 border">{fund.mapper4}</td>
              <td className="p-2 border text-green-600">{fund.success}</td>
              <td className="p-2 border text-red-500">{fund.failed}</td>
              <td className="p-2 border space-x-2">
                <button onClick={() => openEditModal(idx)} className="text-blue-500">Edit</button>
                <button onClick={() => openDeleteModal(idx)} className="text-red-500">Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {/* Modal Reusable */}
      {(showAddModal || showEditModal) && (
        <div className="fixed inset-0 bg-black bg-opacity-30 flex items-center justify-center z-50">
          <div className="bg-white p-6 rounded shadow-lg w-full max-w-lg">
            <h2 className="text-lg font-semibold mb-4">
              {showAddModal ? "Add Configuration" : "Edit Configuration"}
            </h2>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm">Fund House</label>
                <select
                  name="house"
                  value={formData.house || ""}
                  onChange={handleChange}
                  className="w-full border px-2 py-1 rounded"
                  disabled={showEditModal}
                >
                  <option value="">Select House</option>
                  {fundHouseOptions.map((opt) => (
                    <option key={opt} value={opt}>{opt}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-sm">Fund Name</label>
                <select
                  name="name"
                  value={formData.name || ""}
                  onChange={handleChange}
                  className="w-full border px-2 py-1 rounded"
                  disabled={showEditModal}
                >
                  <option value="">Select Name</option>
                  {fundNameOptions.map((opt) => (
                    <option key={opt} value={opt}>{opt}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-sm">Type</label>
                <select
                  name="type"
                  value={formData.type || ""}
                  onChange={handleChange}
                  className="w-full border px-2 py-1 rounded"
                  disabled={showEditModal}
                >
                  <option value="">Select Type</option>
                  {fundTypeOptions.map((opt) => (
                    <option key={opt} value={opt}>{opt}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-sm">Config URL</label>
                <input
                  name="configUrl"
                  value={formData.configUrl || ""}
                  onChange={handleChange}
                  className="w-full border px-2 py-1 rounded"
                />
              </div>
              {["mapper1", "mapper2", "mapper3", "mapper4"].map((field) => (
                <div key={field}>
                  <label className="block text-sm">{field.replace("mapper", "Data Mapper ")}</label>
                  <input
                    name={field}
                    value={formData[field] || ""}
                    onChange={handleChange}
                    className="w-full border px-2 py-1 rounded"
                  />
                </div>
              ))}
            </div>
            <div className="flex justify-end gap-3 mt-6">
              <button onClick={closeModals} className="px-4 py-2 bg-gray-100 rounded hover:bg-gray-200">
                Cancel
              </button>
              <button
                onClick={showAddModal ? handleAdd : handleEdit}
                className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
              >
                {showAddModal ? "Add" : "Update"}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Delete Modal */}
      {showDeleteModal && (
        <div className="fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center z-50">
          <div className="bg-white p-6 rounded shadow-md w-full max-w-sm">
            <p className="text-gray-700 mb-4">Are you sure you want to delete this configuration?</p>
            <div className="flex justify-end gap-3">
              <button
                onClick={closeModals}
                className="px-4 py-2 bg-gray-100 rounded hover:bg-gray-200"
              >
                Cancel
              </button>
              <button
                onClick={handleDelete}
                className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700"
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default MutualFundConfigTable;
