unit GenericConstraints;

interface

type
  TClassConstrained<T: class> = class
  end;

  TRecordConstrained<T: record> = class
  end;

  TConstructorConstrained<T: constructor> = class
  end;

  TInterfaceConstrained<T: interface> = class
  end;

  TUnmanagedConstrained<T: unmanaged> = class
  end;

  TMultipleConstraints<T: class, constructor> = class
  end;

  TInterfaceAndConstructor<T: interface, constructor> = class
  end;

  TUnmanagedAndRecord<T: record, unmanaged> = class
  end;

implementation

end.
